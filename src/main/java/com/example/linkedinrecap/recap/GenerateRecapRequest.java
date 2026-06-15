package com.example.linkedinrecap.recap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GenerateRecapRequest(
        @NotBlank @Size(max = 4000) String eventDescription,
        @Size(max = 5000) String photoNotes,
        @Size(max = 6000) String referencePosts,
        @Size(max = 2000) String structurePreference,
        List<String> photoAssetIds,
        @Size(max = 500) String targetAudience,
        @NotNull PostLength length,
        boolean includeHashtags,
        @NotNull LanguageMode languageMode,
        @Min(0) @Max(100) int englishRatio
) {
}
