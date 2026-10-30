package com.hanviet.etymoai.ai.provider

/**
 * Loại nhà cung cấp AI hỗ trợ mô hình Bring-Your-Own-Key (BYOK)
 */
enum class AiProviderType(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val requiresApiKey: Boolean = true
) {
    OPENAI(
        displayName = "OpenAI (GPT-4o / GPT-4o-mini / o3-mini)",
        defaultBaseUrl = "https://api.openai.com/v1/",
        defaultModel = "gpt-4o-mini"
    ),
    GEMINI(
        displayName = "Google Gemini (2.5 / 2.0 / 1.5 Flash)",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta/",
        defaultModel = "gemini-2.0-flash"
    ),
    DEEPSEEK(
        displayName = "DeepSeek (V3 / R1)",
        defaultBaseUrl = "https://api.deepseek.com/v1/",
        defaultModel = "deepseek-chat"
    ),
    CLAUDE(
        displayName = "Anthropic Claude (3.7 Sonnet / 3.5 Haiku)",
        defaultBaseUrl = "https://api.anthropic.com/v1/",
        defaultModel = "claude-3-5-haiku-20241022"
    ),
    LOCAL_GGUF(
        displayName = "Tùy biến / Cục bộ (Ollama / OpenRouter / vLLM)",
        defaultBaseUrl = "http://localhost:11434/v1/",
        defaultModel = "qwen2.5:latest",
        requiresApiKey = false
    );

    companion object {
        fun fromString(name: String?): AiProviderType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GEMINI
        }

        fun getRecommendedModels(provider: AiProviderType): List<String> = when (provider) {
            GEMINI -> listOf(
                "gemini-2.5-flash",
                "gemini-2.5-pro",
                "gemini-2.0-flash",
                "gemini-2.0-flash-lite",
                "gemini-1.5-flash",
                "gemini-1.5-pro"
            )
            OPENAI -> listOf(
                "gpt-4o",
                "gpt-4o-mini",
                "o3-mini",
                "o1",
                "o1-mini"
            )
            DEEPSEEK -> listOf(
                "deepseek-chat",
                "deepseek-reasoner"
            )
            CLAUDE -> listOf(
                "claude-3-7-sonnet-20250219",
                "claude-3-5-sonnet-20241022",
                "claude-3-5-haiku-20241022"
            )
            LOCAL_GGUF -> listOf(
                "qwen2.5:latest",
                "deepseek-r1:8b",
                "llama3.3:latest",
                "mistral:latest"
            )
        }
    }
}

/**
 * Mục đích truy vấn AI
 */
enum class AiQueryType {
    ETYMOLOGY_EXPLANATION,
    SYNONYM_COMPARISON
}

/**
 * Phân đoạn Hán tự đã được neo (grounded) từ database lõi phục vụ sinh prompt
 */
data class HanjaSegmentPromptInfo(
    val syllable: String,
    val hanjaChar: String?,
    val readingVi: String?,
    val meaningVi: String?
)

/**
 * Câu lệnh đã được neo chặt chẽ với dữ liệu từ điển thực tế (Grounding Context)
 */
data class GroundedPrompt(
    val headword: String,
    val primaryMeaning: String,
    val originType: String,
    val hanjaSegments: List<HanjaSegmentPromptInfo> = emptyList(),
    val queryType: AiQueryType = AiQueryType.ETYMOLOGY_EXPLANATION,
    val targetSynonymHeadword: String? = null,
    val targetSynonymMeaning: String? = null
)

/**
 * Kết quả phân tích giải nghĩa từ nguyên chi tiết từ AI
 */
data class AiEtymologyExplanation(
    val headword: String,
    val etymologySummaryVi: String,
    val deepNuanceExplanationVi: String,
    val culturalContextVi: String? = null,
    val mnemonicsTipVi: String? = null,
    val hasUnverifiedHanja: Boolean = false,
    val rawJson: String? = null
)

/**
 * Kết quả so sánh sắc thái từ đồng nghĩa từ AI
 */
data class AiSynonymComparison(
    val wordA: String,
    val wordB: String,
    val differenceVi: String,
    val usageNotesVi: String,
    val exampleSentenceA: String? = null,
    val exampleSentenceB: String? = null,
    val rawJson: String? = null
)

/**
 * Giao diện trừu tượng chuẩn hóa các yêu cầu gọi mô hình AI đa nhà cung cấp
 */
interface AiProvider {
    val providerType: AiProviderType

    /**
     * Giải thích từ nguyên chuyên sâu dựa trên dữ liệu neo
     */
    suspend fun explainEtymology(prompt: GroundedPrompt): Result<AiEtymologyExplanation>

    /**
     * So sánh sắc thái ngữ nghĩa giữa 2 từ đồng nghĩa/gần nghĩa
     */
    suspend fun compareSynonyms(prompt: GroundedPrompt): Result<AiSynonymComparison>
}
