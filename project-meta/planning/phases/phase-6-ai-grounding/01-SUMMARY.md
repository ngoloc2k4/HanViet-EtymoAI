# Summary Plan 6.1: Unified BYOK AI Provider & Secure Key Storage

## Hoàn thành
- **AiProvider Interface**: Chuẩn hóa đa nhà cung cấp: OpenAI, Google Gemini, Anthropic Claude, DeepSeek, và mô hình cục bộ (Ollama Local GGUF).
- **SecureApiKeyStorage**: Mã hóa đối xứng AES-256-GCM bảo vệ tuyệt đối API Key cá nhân của người dùng, không bao giờ lộ dưới dạng plain text.
- **OpenAiCompatibleClient**: Client gọi API tương thích chuẩn OpenAI Chat Completions với response_format `json_object`, hỗ trợ OpenAI, DeepSeek, Together.ai, Groq, Ollama.
- **GeminiClient**: Client gọi Google Gemini 1.5 Flash/Pro REST API với native `systemInstruction` và `responseMimeType: "application/json"`.
- **Unit Tests**: `ApiKeyStorageTest.kt` và `AiProviderClientTest.kt` với mock HTTP transport offline.
