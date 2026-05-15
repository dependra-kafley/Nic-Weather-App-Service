package com.example.weathermap.service.imd;

import com.example.weathermap.config.ImdSchedulerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles the "try-until-fresh" pattern for daily IMD data.
 * <p>
 * At 9 AM the daily scheduler trigger fires. This component attempts to
 * fetch daily data and checks whether the date field in the response
 * matches today's date. If not (the API hasn't published today's data yet),
 * it schedules a retry every {@code dailyRetryIntervalMs} milliseconds until
 * fresh data arrives or {@code dailyMaxRetries} is exhausted.
 */
@Component
public class DailyDataFreshnessChecker {

    private static final Logger log = LoggerFactory.getLogger(DailyDataFreshnessChecker.class);

    private final ImdWeatherDataRefreshService refreshService;
    private final ImdWeatherDataCache cache;
    private final ImdSchedulerProperties props;

    private final ScheduledExecutorService retryExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "daily-data-retry");
                t.setDaemon(true);
                return t;
            });

    /** Tracks the currently scheduled retry future so we can cancel it once fresh. */
    private volatile ScheduledFuture<?> retryFuture;

    public DailyDataFreshnessChecker(
            ImdWeatherDataRefreshService refreshService,
            ImdWeatherDataCache cache,
            ImdSchedulerProperties props
    ) {
        this.refreshService = refreshService;
        this.cache = cache;
        this.props = props;
    }

    /**
     * Called by the scheduler at 9 AM every day.
     * Cancels any lingering retry from a previous day, then attempts the first fetch.
     * Schedules retries if data is not yet fresh.
     */
    public void onDailyScheduleTrigger() {
        log.info("Daily IMD data trigger fired (9 AM). Starting freshness check.");
        cancelPendingRetry();

        if (attemptDailyFetch()) {
            log.info("Daily IMD data is already fresh on first attempt.");
            return;
        }

        scheduleRetries();
    }

    /**
     * Fetches daily data and checks whether the returned date equals today.
     *
     * @return {@code true} if fresh (date == today), {@code false} otherwise.
     */
    private boolean attemptDailyFetch() {
        try {
            refreshService.refreshDailyFromImd();
        } catch (Exception ex) {
            log.warn("Daily IMD fetch attempt failed with exception: {}", ex.getMessage());
            return false;
        }

        String dateStr = cache.getLastDailyDateStr();
        if (dateStr == null || dateStr.isBlank() || "—".equals(dateStr)) {
            log.info("Daily fetch returned no date value – data not yet available.");
            return false;
        }

        LocalDate today = LocalDate.now();
        try {
            LocalDate dataDate = parseFlexibleDate(dateStr);
            if (today.equals(dataDate)) {
                log.info("Fresh daily IMD data confirmed for {} (date={})", today, dateStr);
                return true;
            } else {
                log.info("Daily IMD data is stale: data date={}, today={}. Will retry in {} ms.",
                        dateStr, today, props.dailyRetryIntervalMs());
                return false;
            }
        } catch (Exception ex) {
            log.warn("Could not parse daily date '{}': {}. Treating as stale.", dateStr, ex.getMessage());
            return false;
        }
    }

    private void scheduleRetries() {
        long intervalMs = props.dailyRetryIntervalMs();
        int maxRetries = props.dailyMaxRetries();
        AtomicInteger attempts = new AtomicInteger(0);

        log.info("Scheduling daily IMD data retry every {} ms, max {} attempts.", intervalMs, maxRetries);

        retryFuture = retryExecutor.scheduleWithFixedDelay(() -> {
            int attempt = attempts.incrementAndGet();
            log.info("Daily IMD retry attempt {}/{}", attempt, maxRetries);

            boolean fresh = attemptDailyFetch();

            if (fresh) {
                log.info("Daily IMD data is now fresh after {} retry attempt(s). Stopping retries.", attempt);
                cancelPendingRetry();
            } else if (attempt >= maxRetries) {
                log.warn("Reached max daily IMD retries ({}). Stopping until next 9 AM trigger.", maxRetries);
                cancelPendingRetry();
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void cancelPendingRetry() {
        if (retryFuture != null && !retryFuture.isDone()) {
            retryFuture.cancel(false);
            log.debug("Cancelled pending daily IMD retry future.");
        }
        retryFuture = null;
    }

    /**
     * Parses common IMD date formats: {@code yyyy-MM-dd}, {@code dd-MM-yyyy},
     * {@code dd/MM/yyyy}, {@code yyyy/MM/dd}.
     */
    private static LocalDate parseFlexibleDate(String dateStr) {
        String s = dateStr.trim();
        // Try ISO first
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ignored) {
            // ignored
        }
        // Try dd-MM-yyyy
        try {
            return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException ignored) {
            // ignored
        }
        // Try dd/MM/yyyy
        try {
            return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
            // ignored
        }
        // Try yyyy/MM/dd
        return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
}
