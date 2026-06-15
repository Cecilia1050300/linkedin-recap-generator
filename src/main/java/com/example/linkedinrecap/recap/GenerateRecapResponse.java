package com.example.linkedinrecap.recap;

public record GenerateRecapResponse(
        String post,
        String provider,
        String modelId,
        String prompt
) {
}
