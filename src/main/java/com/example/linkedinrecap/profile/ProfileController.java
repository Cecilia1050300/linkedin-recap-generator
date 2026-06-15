package com.example.linkedinrecap.profile;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    ProfileSettings getProfile() {
        return profileService.get();
    }

    @PutMapping
    ProfileSettings updateProfile(@Valid @RequestBody ProfileSettings settings) {
        return profileService.update(settings);
    }
}
