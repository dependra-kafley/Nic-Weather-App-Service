package com.example.weathermap.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ImdApiProperties.class, ImdSchedulerProperties.class})
public class AppConfig {

    @Bean
    RestClientCustomizer imdRestClientCustomizer(ImdApiProperties imdApiProperties) {
        return builder -> builder.requestFactory(requestFactory(imdApiProperties));
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    private static SimpleClientHttpRequestFactory requestFactory(ImdApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
