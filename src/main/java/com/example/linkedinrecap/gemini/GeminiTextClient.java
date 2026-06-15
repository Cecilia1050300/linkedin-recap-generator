package com.example.linkedinrecap.gemini;

import com.example.linkedinrecap.config.GeminiProperties;
import com.example.linkedinrecap.watsonx.GenerationResult;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GeminiTextClient {

    private final GeminiProperties properties;
    private final RestClient restClient;

    public GeminiTextClient(GeminiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public GenerationResult generate(String prompt) {
        if (!StringUtils.hasText(properties.apiKey())) {
            if (properties.mockWhenMissing()) {
                return new GenerationResult("Gemini fallback is not configured. Set GEMINI_API_KEY.", "gemini-mock", properties.model());
            }
            throw new IllegalStateException("Gemini is not configured. Set GEMINI_API_KEY.");
        }

        URI uri = URI.create("%s/v1beta/models/%s:generateContent"
                .formatted(properties.baseUrl(), properties.model()));
        Map<String, Object> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", properties.apiKey())
                .body(Map.of(
                        "contents", List.of(Map.of(
                                "parts", List.of(Map.of("text", prompt))
                        )),
                        "generationConfig", Map.of(
                                "temperature", 0.7,
                                "maxOutputTokens", 2400
                        )
                ))
                .retrieve()
                .body(Map.class);

        return new GenerationResult(extractText(response), "gemini-fallback", properties.model());
    }

    private String extractText(Map<String, Object> response) {
        if (response == null || !(response.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini response did not include candidates.");
        }
        Object first = candidates.get(0);
        if (!(first instanceof Map<?, ?> candidate) || !(candidate.get("content") instanceof Map<?, ?> content)) {
            throw new IllegalStateException("Gemini response did not include content.");
        }
        if (!(content.get("parts") instanceof List<?> parts)) {
            throw new IllegalStateException("Gemini response did not include content parts.");
        }
        String text = parts.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(part -> part.get("text"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .reduce("", (left, right) -> left + right)
                .trim();
        if (text.length() < 400) {
            throw new IllegalStateException("Gemini returned an incomplete post. Please retry generation.");
        }
        return text;
    }
}
