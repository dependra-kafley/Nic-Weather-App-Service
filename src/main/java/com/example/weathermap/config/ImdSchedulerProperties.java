package com.example.weathermap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.imd.scheduler")
public record ImdSchedulerProperties(
        boolean enabled,
        String nowcastCron,
        String dailyCron,
        long dailyRetryIntervalMs,
        int dailyMaxRetries
) {

    public ImdSchedulerProperties {
        if (nowcastCron == null || nowcastCron.isBlank()) {
            nowcastCron = "0 0 0/3 * * *";
        }
        if (dailyCron == null || dailyCron.isBlank()) {
            dailyCron = "0 0 1 * * *";
        }
        if (dailyRetryIntervalMs <= 0) {
            dailyRetryIntervalMs = 300_000L;
        }
        if (dailyMaxRetries <= 0) {
            dailyMaxRetries = 12;
        }
    }
}
