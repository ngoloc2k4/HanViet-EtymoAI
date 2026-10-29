---
phase: 6
plan: 1
wave: 1
depends_on: ["1.2", "3.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/ai/provider/AiProvider.kt"
  - "app/src/main/java/com/hanviet/etymoai/ai/provider/OpenAiCompatibleClient.kt"
  - "app/src/main/java/com/hanviet/etymoai/ai/provider/GeminiClient.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Giao diện AiProvider thống nhất hỗ trợ đa nhà cung cấp (OpenAI, Gemini, Anthropic Claude, DeepSeek)."
    - "Cơ chế BYOK (Bring Your Own Key) mã hóa an toàn khóa API trong Android EncryptedSharedPreferences."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/ai/provider/AiProvider.kt"
    - "app/src/main/java/com/hanviet/etymoai/ai/provider/OpenAiCompatibleClient.kt"
---

# Plan 6.1: Giao Diện Tích Hợp AI Đa Nhà Cung Cấp & BYOK (Unified BYOK Provider)

<objective>
Thiết kế giao diện trừu tượng `AiProvider` chuẩn hóa các yêu cầu gọi mô hình ngôn ngữ lớn (LLM), hỗ trợ mô hình tự cung cấp khóa cá nhân (BYOK - Bring Your Own Key) cho OpenAI, Google Gemini, Anthropic Claude, DeepSeek, và lưu trữ an toàn khóa API bằng Android Security Crypto (MasterKeys).

Mục đích: Cho phép người dùng linh hoạt lựa chọn nhà cung cấp AI với chi phí rẻ, tốc độ cao và bảo vệ quyền riêng tư tối đa.
Đầu ra: Interface `AiProvider.kt`, các client triển khai `OpenAiCompatibleClient.kt`, `GeminiClient.kt`, và kho khóa bảo mật.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 1. Tầng Trí tuệ nhân tạo AILayer)
- project-meta/spec/GLOSSARY.md (Khái niệm BYOK)
</context>

<tasks>

<task type="auto">
  <name>Thiết kế Interface AiProvider và Quản lý API Key an toàn</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/ai/provider/AiProvider.kt
    - app/src/main/java/com/hanviet/etymoai/ai/security/ApiKeyStorage.kt
  </files>
  <action>
    Triển khai cấu trúc AI Core:
    1. `interface AiProvider`:
       - `suspend fun explainEtymology(prompt: GroundedPrompt): Result<AiEtymologyExplanation>`
       - `suspend fun compareSynonyms(prompt: GroundedPrompt): Result<AiSynonymComparison>`
    2. `enum class AiProviderType`: `OPENAI`, `GEMINI`, `CLAUDE`, `DEEPSEEK`, `LOCAL_GGUF`.
    3. `ApiKeyStorage`:
       - Sử dụng `EncryptedSharedPreferences.create(...)` với `MasterKey.DEFAULT_AES256_GCM_SPEC`.
       - Lưu trữ API Key riêng biệt cho từng provider; không bao giờ lưu khóa dưới dạng plain text.
       - Cung cấp API xóa khóa tức thì khi người dùng muốn đăng xuất hoặc thu hồi.
  </action>
  <verify>Viết unit test xác nhận đọc/ghi API Key mã hóa thành công.</verify>
  <done>Hạ tầng quản lý khóa API bảo mật hoàn thành theo tiêu chuẩn Android Jetpack Security.</done>
</task>

<task type="auto">
  <name>Hiện thực hóa OpenAiCompatibleClient và GeminiClient</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/ai/provider/OpenAiCompatibleClient.kt
    - app/src/main/java/com/hanviet/etymoai/ai/provider/GeminiClient.kt
  </files>
  <action>
    Triển khai các client gọi API bằng OkHttp / Ktor nhẹ nhàng:
    1. `OpenAiCompatibleClient`:
       - Tương thích với định dạng chuẩn OpenAI Chat Completions API (dùng được cho cả OpenAI, DeepSeek, Together.ai, Groq, Ollama LAN server).
       - Cấu hình request timeout hợp lý (15s), stream response hoặc non-stream.
    2. `GeminiClient`:
       - Tương thích với Google Gemini Developer REST API (Gemini 1.5 Flash / Pro).
       - Hỗ trợ truyền system instruction và JSON response schema (`application/json`).
  </action>
  <verify>Viết mock server test kiểm tra parse payload trả về từ OpenAI và Gemini định dạng JSON.</verify>
  <done>Client hoạt động ổn định, bắt lỗi mạng và quota limit nhẹ nhàng.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Chuyển đổi qua lại giữa các nhà cung cấp OpenAI, Gemini, DeepSeek mượt mà.
- [ ] Khóa API được bảo vệ nghiêm ngặt trong keystore hệ thống.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng mock tests.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
