package com.example.linkedinrecap.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoAssetService {

    private final Path uploadRoot = Path.of("uploads", "photos").toAbsolutePath().normalize();
    private final Map<String, PhotoAsset> photos = new ConcurrentHashMap<>();

    public List<PhotoAsset> saveAll(List<MultipartFile> photos) throws IOException {
        Files.createDirectories(uploadRoot);
        return photos.stream()
                .filter(photo -> !photo.isEmpty())
                .map(this::save)
                .toList();
    }

    private PhotoAsset save(MultipartFile photo) {
        String originalFilename = StringUtils.cleanPath(
                photo.getOriginalFilename() == null ? "photo" : photo.getOriginalFilename()
        );
        String extension = extensionOf(originalFilename);
        String id = UUID.randomUUID().toString();
        Path target = uploadRoot.resolve(id + extension).normalize();
        try {
            photo.transferTo(target);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save uploaded photo: " + originalFilename, exception);
        }
        var asset = new PhotoAsset(
                id,
                originalFilename,
                photo.getContentType(),
                photo.getSize(),
                target.toString()
        );
        photos.put(id, asset);
        return asset;
    }

    public List<PhotoAsset> findAllByIds(List<String> ids) {
        return ids.stream()
                .map(photos::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot);
    }
}
