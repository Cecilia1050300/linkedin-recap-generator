# LinkedIn Recap Generator

Spring Boot MVP for generating LinkedIn activity recap posts with IBM watsonx.ai.

## MVP scope

- Configure personal tone, writing habits, and target audience.
- Upload onsite photos and analyze them with Gemini Vision as event context.
- Choose Chinese, English, or a mixed-language post with an adjustable English ratio.
- Input an event description and generate a LinkedIn-ready recap draft.
- Call IBM watsonx.ai text generation when credentials are configured.
- Fall back to a mock generation when credentials are missing, so the demo still works.
- Publish through the LinkedIn Posts API adapter, defaulting to dry-run.

## Tech stack

- Java 17+
- Spring Boot 3.5.14
- Spring Web, Bean Validation, RestClient
- Static HTML/CSS/JavaScript frontend served by Spring Boot

Spring Boot 3.5 is used here because it is stable and familiar for enterprise interviews. For a longer-lived production service, plan a Spring Boot 4 upgrade after validating dependencies.

## Run locally

Install Java 21 and Maven, then run:

```powershell
mvn test
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

Without environment variables, the app returns a mock LinkedIn post so you can demo the full flow immediately.

## Container build

```powershell
docker build -t linkedin-recap-generator .
docker run --rm -p 8080:8080 linkedin-recap-generator
```

For IBM Cloud Code Engine, build and push the image to IBM Cloud Container Registry, then create an app with the environment variables above.

## Configure watsonx.ai

Copy `.env.example` values into your shell or IDE run configuration:

```powershell
$env:WATSONX_API_KEY="..."
$env:WATSONX_PROJECT_ID="..."
$env:WATSONX_BASE_URL="https://us-south.ml.cloud.ibm.com"
$env:WATSONX_MODEL_ID="ibm/granite-3-8b-instruct"
$env:WATSONX_API_VERSION="2024-03-14"
```

The app obtains an IBM Cloud IAM bearer token, then calls:

```text
POST /ml/v1/text/generation?version=2024-03-14
```

## Configure LinkedIn posting

The adapter uses:

```text
POST https://api.linkedin.com/rest/posts
```

Required settings:

```powershell
$env:LINKEDIN_ACCESS_TOKEN="..."
$env:LINKEDIN_AUTHOR_URN="urn:li:person:..."
$env:LINKEDIN_API_VERSION="202605"
$env:LINKEDIN_DRY_RUN="false"
```

Keep `LINKEDIN_DRY_RUN=true` while testing. LinkedIn posting requires approved OAuth scopes such as `w_member_social` or organization permissions, depending on the author URN.

## API

### Save profile

```http
PUT /api/profile
Content-Type: application/json

{
  "tone": "溫暖、真誠、工程師式反思",
  "writingHabits": "三個收穫加一個問題",
  "audience": "Java 工程師與技術主管"
}
```

### Generate recap

```http
POST /api/recaps/generate
Content-Type: application/json

{
  "eventDescription": "今天參加 IBM Java 工程師面試準備活動，練習 Spring Boot REST API 與 watsonx 串接。",
  "photoNotes": "照片重點：講者提到 Spring Boot Actuator、IBM Cloud Code Engine、prompt engineering。",
  "photoAssetIds": ["uploaded-photo-id"],
  "targetAudience": "面試官與 Java 工程主管",
  "length": "MEDIUM",
  "includeHashtags": true,
  "languageMode": "MIXED",
  "englishRatio": 35
}
```

### Upload onsite photos

```http
POST /api/event-assets/photos
Content-Type: multipart/form-data

photos=<one-or-more-image-files>
```

The current MVP stores photos and lets you paste OCR/Gemini notes into the generation prompt. The next upgrade is to add a direct vision/OCR analyzer behind the same uploaded photo IDs.

### Analyze uploaded photos with Gemini

```http
POST /api/event-assets/photos/analyze
Content-Type: application/json

{
  "photoAssetIds": ["uploaded-photo-id"]
}
```

Set `GEMINI_API_KEY` to call Gemini. Without it, the app returns a mock analysis so the UI flow is still demoable.

### Publish

```http
POST /api/linkedin/publish
Content-Type: application/json

{
  "commentary": "LinkedIn post content..."
}
```

## Interview talking points

- Clear separation between controller, service, prompt builder, and external client.
- Environment-based configuration for cloud credentials.
- Dry-run publishing to avoid accidental LinkedIn posts.
- Mock fallback for demo reliability.
- Photo upload is separated from writing generation, so direct OCR/vision can be added later without changing the recap API.
- Validation and centralized API error handling.
- Next steps: persist profiles in a database, add OAuth login, add streaming generation, deploy to IBM Cloud Code Engine.
