package com.example.linkedinrecap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.linkedin")
public record LinkedInProperties(
        String accessToken,
        String authorUrn,
        String apiVersion,
        boolean dryRun
) {
}
