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

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ImdHttpFetcher(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> fetch(String baseUrl, String queryParamName, String queryParamValue, Class<T> type) {
        String url = baseUrl + "?" + queryParamName + "=" + queryParamValue;
        try {
            String body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                log.warn("IMD empty response url={}", url);
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(body.stripLeading());
            if (root.isArray()) {
                if (root.isEmpty()) {
                    return Optional.empty();
                }
                root = root.get(0);
            }
            return Optional.of(objectMapper.treeToValue(root, type));
        } catch (RestClientException e) {
            log.error("IMD HTTP error url={}: {}", url, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("IMD parse error url={}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }
}
