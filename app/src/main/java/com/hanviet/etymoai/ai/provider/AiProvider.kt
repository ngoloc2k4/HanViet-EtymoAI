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
        displayName = "OpenAI (GPT-4o Mini / GPT-4o)",
        defaultBaseUrl = "https://api.openai.com/v1/",
        defaultModel = "gpt-4o-mini"
    ),
    GEMINI(
        displayName = "Google Gemini (1.5 Flash / Pro)",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta/",
        defaultModel = "gemini-1.5-flash"
    ),
    DEEPSEEK(
        displayName = "DeepSeek (V3 / R1)",
        defaultBaseUrl = "https://api.deepseek.com/v1/",
        defaultModel = "deepseek-chat"
    ),
    CLAUDE(
        displayName = "Anthropic Claude (Haiku / Sonnet)",
        defaultBaseUrl = "https://api.anthropic.com/v1/",
        defaultModel = "claude-3-haiku-20240307"
    ),
    LOCAL_GGUF(
        displayName = "Mô hình cục bộ (Ollama / Local)",
        defaultBaseUrl = "http://localhost:11434/v1/",
        defaultModel = "qwen2.5:latest",
        requiresApiKey = false
    );

    companion object {
        fun fromString(name: String?): AiProviderType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GEMINI
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
