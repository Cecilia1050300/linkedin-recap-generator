package com.example.linkedinrecap.watsonx;

import com.example.linkedinrecap.config.WatsonxProperties;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class WatsonxClient {

    private final WatsonxProperties properties;
    private final RestClient restClient;

    public WatsonxClient(WatsonxProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public GenerationResult generate(String prompt) {
        if (!isConfigured()) {
            if (properties.mockWhenMissing()) {
                return new GenerationResult(mockPost(), "mock", properties.modelId());
            }
            throw new IllegalStateException("watsonx is not configured. Set WATSONX_API_KEY and WATSONX_PROJECT_ID.");
        }

        String token = fetchIamToken();
        URI uri = URI.create(properties.baseUrl() + "/ml/v1/text/generation?version=" + properties.apiVersion());
        Map<String, Object> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .body(Map.of(
                        "model_id", properties.modelId(),
                        "project_id", properties.projectId(),
                        "input", prompt,
                        "parameters", Map.of(
                                "decoding_method", "greedy",
                                "max_new_tokens", 650,
                                "min_new_tokens", 120,
                                "repetition_penalty", 1.08
                        )
                ))
                .retrieve()
                .body(Map.class);

        return new GenerationResult(extractGeneratedText(response), "watsonx.ai", properties.modelId());
    }

    private boolean isConfigured() {
        return StringUtils.hasText(properties.apiKey()) && StringUtils.hasText(properties.projectId());
    }

    private String fetchIamToken() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "urn:ibm:params:oauth:grant-type:apikey");
        form.add("apikey", properties.apiKey());

        Map<String, Object> response = restClient.post()
                .uri(properties.iamUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        Object token = response == null ? null : response.get("access_token");
        if (token == null) {
            throw new IllegalStateException("IBM Cloud IAM did not return an access_token.");
        }
        return token.toString();
    }

    private String extractGeneratedText(Map<String, Object> response) {
        if (response == null || !(response.get("results") instanceof List<?> results) || results.isEmpty()) {
            throw new IllegalStateException("watsonx response did not include generated results.");
        }
        Object first = results.get(0);
        if (first instanceof Map<?, ?> result && result.get("generated_text") != null) {
            return result.get("generated_text").toString().trim();
        }
        throw new IllegalStateException("watsonx response did not include generated_text.");
    }

    private String mockPost() {
        return """
                今天把 Spring Boot、外部 API 串接和雲端 AI 服務放在同一個小專案裡練習，最大的收穫是：MVP 不只是把功能做出來，而是先把風險邊界想清楚。

                這次我實作了一個 LinkedIn 活動 recap 生成器：

                1. 先設定自己的語氣與撰寫習慣，讓輸出不只是通用 AI 文案。
                2. 透過 Spring Boot API 把活動描述轉成結構化 prompt。
                3. 串接 IBM watsonx.ai 產生可直接修改、發布的 LinkedIn 草稿。

                我也刻意把 LinkedIn 發文設計成 dry-run first，因為真實產品裡，權限、審核與誤發文風險都需要被認真處理。

                對我來說，這個練習不只是熟 Spring Boot，而是在練習如何把一個模糊需求拆成能 demo、能擴充、也能被討論的系統。

                如果你在做面試專案，你會優先展示功能完整度，還是架構取捨？

                #SpringBoot #Java #IBMWatsonx #LinkedIn #SoftwareEngineering
                """;
    }
}
