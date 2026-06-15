package com.example.linkedinrecap.recap;

import com.example.linkedinrecap.gemini.GeminiTextClient;
import com.example.linkedinrecap.profile.ProfileService;
import com.example.linkedinrecap.watsonx.WatsonxClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Service
public class RecapService {

    private final ProfileService profileService;
    private final PromptBuilder promptBuilder;
    private final WatsonxClient watsonxClient;
    private final GeminiTextClient geminiTextClient;

    public RecapService(
            ProfileService profileService,
            PromptBuilder promptBuilder,
            WatsonxClient watsonxClient,
            GeminiTextClient geminiTextClient
    ) {
        this.profileService = profileService;
        this.promptBuilder = promptBuilder;
        this.watsonxClient = watsonxClient;
        this.geminiTextClient = geminiTextClient;
    }

    public GenerateRecapResponse generate(GenerateRecapRequest request) {
        String prompt = promptBuilder.build(profileService.get(), request);
        var generation = generateWithFallback(prompt);
        return new GenerateRecapResponse(generation.text(), generation.provider(), generation.modelId(), prompt);
    }

    private com.example.linkedinrecap.watsonx.GenerationResult generateWithFallback(String prompt) {
        try {
            return watsonxClient.generate(prompt);
        } catch (RestClientResponseException exception) {
            if (shouldFallbackToGemini(exception)) {
                return geminiTextClient.generate(prompt);
            }
            throw exception;
        }
    }

    private boolean shouldFallbackToGemini(RestClientResponseException exception) {
        String response = exception.getResponseBodyAsString();
        return exception.getStatusCode().is5xxServerError()
                || (exception.getStatusCode().value() == 404 && response.contains("model_not_supported"));
    }
}
