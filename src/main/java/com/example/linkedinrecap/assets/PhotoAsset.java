package com.example.linkedinrecap.assets;

public record PhotoAsset(
        String id,
        String originalFilename,
        String contentType,
        long size,
        String storagePath
) {
}
