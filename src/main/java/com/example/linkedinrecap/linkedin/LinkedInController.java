package com.example.linkedinrecap.linkedin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/linkedin")
public class LinkedInController {

    private final LinkedInService linkedInService;

    public LinkedInController(LinkedInService linkedInService) {
        this.linkedInService = linkedInService;
    }

    @PostMapping("/publish")
    PublishResponse publish(@Valid @RequestBody PublishRequest request) {
        return linkedInService.publish(request);
    }
}
