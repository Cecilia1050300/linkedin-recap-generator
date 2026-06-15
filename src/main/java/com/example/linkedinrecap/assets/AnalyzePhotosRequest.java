package com.example.linkedinrecap.assets;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AnalyzePhotosRequest(
        @NotEmpty List<String> photoAssetIds
) {
}
