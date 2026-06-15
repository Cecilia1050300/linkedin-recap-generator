package com.example.linkedinrecap.assets;

import java.io.IOException;
import java.util.List;
import com.example.linkedinrecap.gemini.GeminiVisionClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/event-assets")
public class PhotoAssetController {

    private static final Logger log = LoggerFactory.getLogger(PhotoAssetController.class);

    private final PhotoAssetService photoAssetService;
    private final GeminiVisionClient geminiVisionClient;

    public PhotoAssetController(PhotoAssetService photoAssetService, GeminiVisionClient geminiVisionClient) {
        this.photoAssetService = photoAssetService;
        this.geminiVisionClient = geminiVisionClient;
    }

    @PostMapping("/photos")
    PhotoUploadResponse uploadPhotos(@RequestParam("photos") List<MultipartFile> photos) throws IOException {
        var savedPhotos = photoAssetService.saveAll(photos);
        log.info("Uploaded {} photo(s) for recap analysis.", savedPhotos.size());
        return new PhotoUploadResponse(savedPhotos);
    }

    @PostMapping("/photos/analyze")
    AnalyzePhotosResponse analyzePhotos(@Valid @RequestBody AnalyzePhotosRequest request) {
        var photos = photoAssetService.findAllByIds(request.photoAssetIds());
        log.info("Analyzing {} uploaded photo(s) with Gemini.", photos.size());
        return new AnalyzePhotosResponse(geminiVisionClient.analyze(photos), "gemini");
    }
}
