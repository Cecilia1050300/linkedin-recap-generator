package com.example.linkedinrecap.gemini;

import com.example.linkedinrecap.assets.PhotoAsset;
import com.example.linkedinrecap.config.GeminiProperties;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GeminiVisionClient {

    private final GeminiProperties properties;
    private final RestClient restClient;

    public GeminiVisionClient(GeminiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public String analyze(List<PhotoAsset> photos) {
        if (photos.isEmpty()) {
            throw new IllegalArgumentException("No uploaded photos were selected for analysis.");
        }
        if (!StringUtils.hasText(properties.apiKey())) {
            if (properties.mockWhenMissing()) {
                return mockAnalysis(photos);
            }
            throw new IllegalStateException("Gemini is not configured. Set GEMINI_API_KEY.");
        }

        URI uri = URI.create("%s/v1beta/models/%s:generateContent"
                .formatted(properties.baseUrl(), properties.model()));
        Map<String, Object> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", properties.apiKey())
                .body(Map.of("contents", List.of(Map.of("parts", buildParts(photos)))))
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    private List<Map<String, Object>> buildParts(List<PhotoAsset> photos) {
        var parts = new java.util.ArrayList<Map<String, Object>>();
        parts.add(Map.of("text", """
                你正在協助使用者整理參訪、講座、workshop 或面試相關活動照片。
                請閱讀所有圖片中的投影片、白板、筆記、標題、流程圖與可辨識文字，整理成可用來寫 LinkedIn recap 的活動素材。

                輸出格式：
                1. 活動主題：
                2. 現場學到的知識點：
                3. 可以寫進 LinkedIn 的具體觀察：
                4. 技術關鍵字：
                5. 不確定或圖片看不清楚的地方：

                請使用繁體中文。不要編造圖片中看不到的資訊。
                """));
        for (PhotoAsset photo : photos) {
            parts.add(Map.of("text", "圖片檔名：" + photo.originalFilename()));
            parts.add(Map.of("inline_data", Map.of(
                    "mime_type", normalizedMimeType(photo.contentType()),
                    "data", base64(photo.storagePath())
            )));
        }
        return parts;
    }

    private String normalizedMimeType(String contentType) {
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "image/jpeg";
    }

    private String base64(String storagePath) {
        try {
            return Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of(storagePath)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read uploaded photo for Gemini analysis.", exception);
        }
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
        return parts.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(part -> part.get("text"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .reduce("", (left, right) -> left + right)
                .trim();
    }

    private String mockAnalysis(List<PhotoAsset> photos) {
        return """
                1. 活動主題：
                從上傳的 %d 張照片整理活動重點。Gemini API 尚未設定，因此目前回傳示範分析。

                2. 現場學到的知識點：
                - Spring Boot REST API 設計與外部服務串接。
                - IBM watsonx.ai 可作為文章生成與摘要整理的模型服務。
                - 上傳照片可以成為 recap 內容素材，後續可加入 OCR/vision pipeline。

                3. 可以寫進 LinkedIn 的具體觀察：
                專案不只是產文工具，也是在練習如何把參訪現場的非結構化素材整理成可分享的專業內容。

                4. 技術關鍵字：
                Spring Boot, IBM watsonx.ai, Gemini Vision, LinkedIn API, OCR, multimodal AI

                5. 不確定或圖片看不清楚的地方：
                目前尚未設定 GEMINI_API_KEY，因此沒有真的讀取圖片內容。
                """.formatted(photos.size());
    }
}
