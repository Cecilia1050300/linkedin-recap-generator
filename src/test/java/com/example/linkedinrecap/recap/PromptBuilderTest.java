package com.example.linkedinrecap.recap;

import com.example.linkedinrecap.profile.ProfileSettings;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void buildsPromptWithProfileAndEventDetails() {
        var profile = new ProfileSettings(
                "真誠、工程師視角",
                "三個收穫加一個問題",
                "Java 工程師"
        );
        var request = new GenerateRecapRequest(
                "參加 IBM Spring Boot workshop，練習 REST API 與 cloud deployment。",
                "照片裡有 Spring Boot Actuator、Code Engine、watsonx prompt engineering 的筆記。",
                "Here are my key takeaways + 中文【活動回顧】。",
                "英文 recap + 3 個 key takeaways + 中文回顧。",
                List.of("photo-1", "photo-2"),
                "面試官與工程主管",
                PostLength.MEDIUM,
                true,
                LanguageMode.MIXED,
                35
        );

        String prompt = promptBuilder.build(profile, request);

        assertThat(prompt)
                .contains("真誠、工程師視角")
                .contains("三個收穫加一個問題")
                .contains("面試官與工程主管")
                .contains("IBM Spring Boot workshop")
                .contains("photo-1, photo-2")
                .contains("Spring Boot Actuator")
                .contains("Here are my key takeaways")
                .contains("英文約佔 35%")
                .contains("3 到 5 個精準 hashtags");
    }
}
