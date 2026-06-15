package com.example.linkedinrecap.linkedin;

import com.example.linkedinrecap.config.LinkedInProperties;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class LinkedInService {

    private final LinkedInProperties properties;
    private final RestClient restClient;

    public LinkedInService(LinkedInProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public PublishResponse publish(PublishRequest request) {
        if (properties.dryRun() || !isConfigured()) {
            return new PublishResponse(
                    "DRY_RUN",
                    null,
                    "LinkedIn publish skipped. Configure LINKEDIN_ACCESS_TOKEN, LINKEDIN_AUTHOR_URN and set LINKEDIN_DRY_RUN=false to post.",
                    request.commentary()
            );
        }

        Map<String, Object> body = Map.of(
                "author", properties.authorUrn(),
                "commentary", request.commentary(),
                "visibility", "PUBLIC",
                "distribution", Map.of(
                        "feedDistribution", "MAIN_FEED",
                        "targetEntities", List.of(),
                        "thirdPartyDistributionChannels", List.of()
                ),
                "lifecycleState", "PUBLISHED",
                "isReshareDisabledByAuthor", false
        );

        restClient.post()
                .uri(URI.create("https://api.linkedin.com/rest/posts"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("LinkedIn-Version", properties.apiVersion())
                .header("X-Restli-Protocol-Version", "2.0.0")
                .headers(headers -> headers.setBearerAuth(properties.accessToken()))
                .body(body)
                .retrieve()
                .toBodilessEntity();

        return new PublishResponse("PUBLISHED", properties.authorUrn(), "Post submitted to LinkedIn.", request.commentary());
    }

    private boolean isConfigured() {
        return StringUtils.hasText(properties.accessToken()) && StringUtils.hasText(properties.authorUrn());
    }
}
