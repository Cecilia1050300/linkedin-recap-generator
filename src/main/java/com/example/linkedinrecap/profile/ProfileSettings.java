package com.example.linkedinrecap.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileSettings(
        @NotBlank @Size(max = 800) String tone,
        @NotBlank @Size(max = 1200) String writingHabits,
        @NotBlank @Size(max = 500) String audience
) {
}
