const byId = (id) => document.getElementById(id);
let uploadedPhotoIds = [];

const status = (message) => {
  byId("status").textContent = message;
};

const photoStatus = (message) => {
  byId("photoList").textContent = message;
  status(message);
};

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options
  });
  const text = await response.text();
  const body = text ? JSON.parse(text) : {};
  if (!response.ok) {
    throw new Error(body.message ?? `Request failed: ${response.status}`);
  }
  return body;
}

byId("saveProfile").addEventListener("click", async () => {
  try {
    const tone = [byId("tonePreset").value, byId("tone").value].filter(Boolean).join("\n補充：");
    await request("/api/profile", {
      method: "PUT",
      body: JSON.stringify({
        tone,
        writingHabits: byId("habits").value,
        audience: byId("audience").value
      })
    });
    status("語氣設定已儲存。");
  } catch (error) {
    status(error.message);
  }
});

byId("languageMode").addEventListener("change", () => {
  byId("englishRatioWrap").classList.toggle("hidden", byId("languageMode").value !== "MIXED");
  applyLanguageTemplate();
});

byId("englishRatio").addEventListener("input", () => {
  byId("englishRatioValue").textContent = `${byId("englishRatio").value}%`;
});

function applyLanguageTemplate() {
  const mode = byId("languageMode").value;
  const template = byId("referencePosts");
  if (template.dataset.touched === "true") {
    return;
  }
  if (mode === "ALL_CHINESE") {
    template.value = `中文標題用 emoji + 活動主題，例如「IBM 高雄參訪心得｜看見企業 AI 與職涯技能培養」。
第一段交代活動、主辦/參訪單位，以及最主要的學習收穫。
中段用 3 個 key takeaways，每點格式是「技術概念：實作內容 + 學到什麼」。
接著寫【活動回顧】，語氣可以熱情，但要保留技術細節。
最後感謝主辦、講者、同行夥伴，並寫一句期待未來應用。
Hashtags 可以混合英文技術詞、活動名稱、產業關鍵字。`;
    return;
  }
  if (mode === "ALL_ENGLISH") {
    template.value = `English title with emoji + event outcome, such as Workshop Recap or Unlocked New Skills.
Open with the event name, host/company, and the most meaningful learning outcome.
Use 3 key takeaways. Each takeaway should include a technical concept, what was practiced, and what was learned.
Close by thanking organizers/speakers/peers and mention how the skills can be applied in future projects.
Use precise hashtags mixing technology, company, cloud, and industry keywords.`;
    return;
  }
  template.value = `英文標題用 emoji + 成果/活動主題，例如 Workshop Recap、Unlocked New Skills、Thrilled to begin。
先寫英文 recap，說明活動名稱、主辦單位、實作成果或名次。
中段用 3 個 key takeaways，每點格式是「技術概念：實作內容 + 學到什麼」。
接著寫中文【活動回顧】，語氣可以熱情，但要保留技術細節。
最後感謝主辦、講者、隊友，並寫一句期待未來應用。
Hashtags 混合英文技術詞、活動名稱、產業關鍵字。`;
}

byId("referencePosts").addEventListener("input", () => {
  byId("referencePosts").dataset.touched = "true";
});

byId("uploadPhotos").addEventListener("click", async () => {
  const files = byId("photos").files;
  if (!files.length) {
    photoStatus("請先選擇照片。");
    return;
  }

  const form = new FormData();
  [...files].forEach((file) => form.append("photos", file));

  try {
    photoStatus("正在上傳照片...");
    const response = await fetch("/api/event-assets/photos", { method: "POST", body: form });
    const text = await response.text();
    const body = text ? JSON.parse(text) : {};
    if (!response.ok) {
      throw new Error(body.message ?? `Upload failed: ${response.status}`);
    }
    uploadedPhotoIds = body.photos.map((photo) => photo.id);
    const uploadedSummary = body.photos
      .map((photo) => `${photo.originalFilename} (${Math.round(photo.size / 1024)} KB)`)
      .join("、");
    byId("photoList").textContent = uploadedSummary;
    status(`已上傳 ${body.photos.length} 張照片。請按「分析照片」。`);
  } catch (error) {
    photoStatus(error.message);
  }
});

byId("analyzePhotos").addEventListener("click", async () => {
  if (!uploadedPhotoIds.length) {
    photoStatus("請先上傳照片，再分析照片。");
    return;
  }

  try {
    byId("analyzePhotos").disabled = true;
    photoStatus(`正在用 Gemini 分析 ${uploadedPhotoIds.length} 張照片，可能需要 10 到 30 秒...`);
    const body = await request("/api/event-assets/photos/analyze", {
      method: "POST",
      body: JSON.stringify({ photoAssetIds: uploadedPhotoIds })
    });
    byId("photoNotes").value = body.notes;
    byId("photoNotes").scrollIntoView({ behavior: "smooth", block: "center" });
    photoStatus(`照片分析完成。provider=${body.provider}`);
  } catch (error) {
    photoStatus(`照片分析失敗：${error.message}`);
  } finally {
    byId("analyzePhotos").disabled = false;
  }
});

byId("generate").addEventListener("click", async () => {
  try {
    status("正在呼叫 watsonx 生成文章...");
    const body = await request("/api/recaps/generate", {
      method: "POST",
      body: JSON.stringify({
        eventDescription: byId("eventDescription").value,
        photoNotes: byId("photoNotes").value,
        referencePosts: byId("referencePosts").value,
        structurePreference: byId("referencePosts").value,
        photoAssetIds: uploadedPhotoIds,
        targetAudience: byId("audience").value,
        length: byId("length").value,
        includeHashtags: byId("hashtags").checked,
        languageMode: byId("languageMode").value,
        englishRatio: Number(byId("englishRatio").value)
      })
    });
    byId("result").value = body.post;
    status(`完成。provider=${body.provider}, model=${body.modelId}`);
  } catch (error) {
    status(error.message);
  }
});

byId("languageMode").dispatchEvent(new Event("change"));

byId("copy").addEventListener("click", async () => {
  await navigator.clipboard.writeText(byId("result").value);
  status("已複製到剪貼簿。");
});

byId("publish").addEventListener("click", async () => {
  try {
    const body = await request("/api/linkedin/publish", {
      method: "POST",
      body: JSON.stringify({ commentary: byId("result").value })
    });
    status(JSON.stringify(body, null, 2));
  } catch (error) {
    status(error.message);
  }
});
