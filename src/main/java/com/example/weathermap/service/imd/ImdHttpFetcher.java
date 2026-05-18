package com.example.weathermap.service.imd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ImdHttpFetcher {

    private static final Logger log = LoggerFactory.getLogger(ImdHttpFetcher.class);
    private static final int MAX_LOG_BODY_CHARS = 4000;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ImdApiCallAudit apiCallAudit;

    public ImdHttpFetcher(RestClient restClient, ObjectMapper objectMapper, ImdApiCallAudit apiCallAudit) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiCallAudit = apiCallAudit;
    }

    public <T> Optional<T> fetch(
            String apiName,
            String baseUrl,
            String queryParamName,
            String queryParamValue,
            Class<T> type
    ) {
        String url = baseUrl + "?" + queryParamName + "=" + queryParamValue;
        apiCallAudit.callStarted(apiName, queryParamValue, false);
        log.info("[{}] Request GET {}", apiName, url);
        try {
            String body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                log.warn("[{}] Empty response for {}", apiName, url);
                apiCallAudit.callFailed(apiName, queryParamValue, false, "empty response body");
                return Optional.empty();
            }
            log.info("[{}] Response JSON: {}", apiName, truncateForLog(body));
            JsonNode root = objectMapper.readTree(body.stripLeading());
            if (root.isArray()) {
                if (root.isEmpty()) {
                    log.warn("[{}] Empty JSON array for {}", apiName, url);
                    apiCallAudit.callFailed(apiName, queryParamValue, false, "empty JSON array");
                    return Optional.empty();
                }
                root = root.get(0);
            }
            apiCallAudit.callSucceeded(apiName, queryParamValue, false);
            return Optional.of(objectMapper.treeToValue(root, type));
        } catch (RestClientException e) {
            log.error("[{}] HTTP error for {} — {}", apiName, url, e.getMessage(), e);
            apiCallAudit.callFailed(apiName, queryParamValue, false, "HTTP: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[{}] Parse error for {} — {}", apiName, url, e.getMessage(), e);
            apiCallAudit.callFailed(apiName, queryParamValue, false, "parse: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static String truncateForLog(String body) {
        String trimmed = body.strip();
        if (trimmed.length() <= MAX_LOG_BODY_CHARS) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_LOG_BODY_CHARS) + "… (truncated)";
    }
}
