package com.example.linkedinrecap.recap;

import com.example.linkedinrecap.profile.ProfileSettings;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PromptBuilder {

    public String build(ProfileSettings profile, GenerateRecapRequest request) {
        String audience = StringUtils.hasText(request.targetAudience())
                ? request.targetAudience()
                : profile.audience();
        String languageInstruction = languageInstruction(request);
        String photoContext = photoContext(request);
        String referenceContext = referenceContext(request);
        return """
                你是一位專業 LinkedIn ghostwriter，請根據使用者的既有文章風格，產出一篇活動 recap 貼文。

                絕對優先事項：
                - 內容要像使用者本人在 LinkedIn 分享技術活動，不要像通用 AI 文案。
                - 具體知識點要來自活動描述與照片分析，不要空泛說「收穫很多」。
                - 可以使用 emoji，但要節制，符合 LinkedIn recap，而不是廣告文案。
                - 不要提到你是 AI，不要說「根據照片」或「根據素材」。

                語言與格式規則：
                - %s
                - 長度偏好：%s。
                - 語言選擇的優先權最高；模板只能參考語氣與段落節奏，不可覆蓋語言規則。
                - 文章要自然、具體、可信，不要過度誇大。
                - 不要編造不存在的講者、公司名稱、數字或成果。
                - 如果照片筆記只是不完整關鍵字，請整理成自然敘事，不要逐條硬列。
                - 保留可直接貼到 LinkedIn 的格式。
                - 必須輸出完整文章，不可只寫開頭；至少包含標題、3 個 key takeaways、活動回顧、感謝結尾、hashtags。
                - %s

                使用者偏好的文章結構：
                %s

                使用者過去文章/模板風格參考：
                %s

                個人語氣：
                %s

                撰寫習慣：
                %s

                目標讀者：
                %s

                活動描述：
                %s

                現場照片與筆記素材：
                %s
                """.formatted(
                languageInstruction,
                request.length(),
                request.includeHashtags() ? "結尾加入 3 到 5 個精準 hashtags。" : "不要加入 hashtags。",
                structurePreference(request),
                referenceContext,
                profile.tone(),
                profile.writingHabits(),
                audience,
                request.eventDescription(),
                photoContext
        );
    }

    private String languageInstruction(GenerateRecapRequest request) {
        return switch (request.languageMode()) {
            case ALL_CHINESE -> "使用繁體中文撰寫。除了 IBM、watsonx、NATC、SkillsBuild、API、AI、Java、Spring Boot 等必要專有名詞外，不要使用英文句子或英文段落。";
            case ALL_ENGLISH -> "Write the post fully in polished professional English. Do not include a Chinese recap section.";
            case MIXED -> "使用中英混合撰寫，英文約佔 %d%%，但總長度仍需符合長度偏好，避免因雙語重複導致文章過長。"
                    .formatted(request.englishRatio());
        };
    }

    private String photoContext(GenerateRecapRequest request) {
        String notes = StringUtils.hasText(request.photoNotes()) ? request.photoNotes() : "無";
        List<String> assetIds = request.photoAssetIds() == null ? List.of() : request.photoAssetIds();
        return """
                已上傳照片 ID：%s
                從照片整理出的重點：
                %s
                """.formatted(assetIds.isEmpty() ? "無" : String.join(", ", assetIds), notes);
    }

    private String structurePreference(GenerateRecapRequest request) {
        if (StringUtils.hasText(request.structurePreference())) {
            return """
                    使用者提供的模板如下。請只學習它的節奏、段落順序、重點整理方式與 LinkedIn 語氣。
                    若模板語言與本次語言設定衝突，必須以本次語言設定為準。
                    %s
                    """.formatted(request.structurePreference());
        }
        if (request.languageMode() == LanguageMode.ALL_CHINESE) {
            return """
                    1. 用一行醒目的中文 LinkedIn 標題開場，可以保留必要英文專有名詞。
                    2. 第一段交代活動、參訪對象與最重要的學習收穫。
                    3. 用 3 個重點整理 key takeaways，每點要包含技術名詞與實作意義。
                    4. 接著寫【活動回顧】，用自然中文補充個人觀察與學習心得。
                    5. 最後感謝主辦/講者/同行夥伴，並用一句期待未來應用的收束。
                    6. hashtags 可以混合英文技術詞與中文產業關鍵字。
                    """;
        }
        return """
                1. 用一行醒目的 LinkedIn 標題開場，可以中英混合，主題要明確。
                2. 英文段落先說活動名稱、主辦單位與最重要成果。
                3. 用 3 個 bullet key takeaways，每點都要有技術名詞與實作意義。
                4. 接著用中文寫「活動回顧」版本，語氣自然，補充個人學習心得。
                5. 最後感謝主辦/講者/隊友，並用一句期待未來應用的收束。
                6. hashtags 要包含活動/技術/雲端或產業關鍵字，不要太泛。
                """;
    }

    private String referenceContext(GenerateRecapRequest request) {
        if (StringUtils.hasText(request.referencePosts())) {
            return request.referencePosts();
        }
        return """
                使用者常見風格：
                - 英文標題常用 emoji + achievement/action，例如「Unlocked New Skills...」「Workshop Recap」「Thrilled to begin...」。
                - 先用英文整理 professional recap，再用中文補一段活動回顧。
                - 喜歡用「Here are my key takeaways」後接 3 個重點。
                - bullet 通常是「技術概念：實作內容 + 學到什麼」。
                - 中文段落偏熱情但不失專業，會使用「收穫滿滿」「實戰體驗」「非常感謝」。
                - 結尾會感謝主辦、技術導師、隊友，並提到期待把技能應用到未來專案。
                - hashtag 會混合英文技術詞與活動/產業關鍵字。
                """;
    }
}
