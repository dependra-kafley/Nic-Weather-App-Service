package com.example.weathermap.service.imd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Structured audit log for every IMD API invocation (HTTP or mock).
 * Logger name {@code imd.api.audit} is written to {@code ./logs/nic-weather-app.log} in local profile.
 */
@Component
public class ImdApiCallAudit {

    private static final Logger audit = LoggerFactory.getLogger("imd.api.audit");

    public void refreshStarted(String refreshType) {
        audit.info("IMD_REFRESH_START type={}", refreshType);
    }

    public void refreshFinished(String refreshType, String outcome, String detail) {
        audit.info("IMD_REFRESH_END type={} outcome={} detail={}", refreshType, outcome, detail);
    }

    public void callStarted(String apiName, String resourceId, boolean mock) {
        audit.info("IMD_CALL_START api={} id={} mock={}", apiName, resourceId, mock);
    }

    public void callSucceeded(String apiName, String resourceId, boolean mock) {
        audit.info("IMD_CALL_OK api={} id={} mock={}", apiName, resourceId, mock);
    }

    public void callFailed(String apiName, String resourceId, boolean mock, String reason) {
        audit.warn("IMD_CALL_FAIL api={} id={} mock={} reason={}", apiName, resourceId, mock, reason);
    }
}
