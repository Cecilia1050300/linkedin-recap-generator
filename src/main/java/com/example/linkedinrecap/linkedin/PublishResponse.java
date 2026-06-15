package com.example.linkedinrecap.linkedin;

public record PublishResponse(
        String status,
        String authorUrn,
        String message,
        String commentary
) {
}
