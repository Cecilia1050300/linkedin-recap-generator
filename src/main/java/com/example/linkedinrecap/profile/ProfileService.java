package com.example.linkedinrecap.profile;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final AtomicReference<ProfileSettings> profile = new AtomicReference<>(
            new ProfileSettings(
                    "溫暖、真誠、帶一點工程師式反思；不要太像業配。",
                    "開頭用一句觀察切入，中段整理 3 個收穫，結尾留下一個問題邀請互動。",
                    "台灣軟體工程師、技術主管、正在學 Spring Boot 的求職者"
            )
    );

    public ProfileSettings get() {
        return profile.get();
    }

    public ProfileSettings update(ProfileSettings settings) {
        profile.set(settings);
        return settings;
    }
}
