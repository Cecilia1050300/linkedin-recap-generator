package com.example.linkedinrecap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.watsonx")
public record WatsonxProperties(
        String apiKey,
        String projectId,
        String baseUrl,
        String iamUrl,
        String modelId,
        String apiVersion,
        boolean mockWhenMissing
) {
}
