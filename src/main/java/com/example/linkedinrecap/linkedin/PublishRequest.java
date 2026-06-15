package com.example.linkedinrecap.linkedin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublishRequest(
        @NotBlank @Size(max = 3000) String commentary
) {
}
