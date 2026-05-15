package com.example.weathermap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MockModeStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(MockModeStartupLogger.class);

    private final ImdApiProperties imdApiProperties;

    public MockModeStartupLogger(ImdApiProperties imdApiProperties) {
        this.imdApiProperties = imdApiProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logMockMode() {
        if (!imdApiProperties.nowcast().mock()) {
            log.info("IMD live API mode — calling mausam.imd.gov.in and city.imd.gov.in");
            return;
        }
        log.warn("============================================================");
        log.warn("  IMD MOCK MODE — using embedded test data (no external calls)");
        log.warn("  Map, rainfall, warnings, and city panels use sample JSON");
        log.warn("  Live APIs: set SPRING_PROFILES_ACTIVE=prod or mock: false");
        log.warn("============================================================");
    }
}
