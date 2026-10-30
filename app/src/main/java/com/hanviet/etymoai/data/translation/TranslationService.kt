package com.hanviet.etymoai.data.translation

import com.hanviet.etymoai.ai.security.ApiKeyStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Các bộ máy dịch thuật hỗ trợ trong ứng dụng
 */
enum class TranslationEngine(
    val id: String,
    val displayName: String,
    val description: String,
    val requiresApiKey: Boolean
) {
    GOOGLE_TRANSLATE(
        id = "GOOGLE_TRANSLATE",
        displayName = "Google Dịch (Không chính thức / Miễn phí)",
        description = "Sử dụng endpoint gtx/dict-chrome-ex miễn phí, dịch nhanh chóng không cần API key.",
        requiresApiKey = false
    ),
    DEEPL(
        id = "DEEPL",
        displayName = "DeepL Translator (API Key)",
        description = "Chất lượng dịch thuật cao cấp với tài khoản DeepL API Free (:fx) hoặc DeepL Pro.",
        requiresApiKey = true
    ),
    AI_GROUNDED(
        id = "AI_GROUNDED",
        displayName = "AI Trợ lý Ngữ cảnh (BYOK)",
        description = "Sử dụng LLM (Gemini, OpenAI, Claude, DeepSeek) kèm dữ liệu từ nguyên Hán-Hàn.",
        requiresApiKey = true
    );

    companion object {
        fun fromId(id: String?): TranslationEngine {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GOOGLE_TRANSLATE
        }
    }
}

/**
 * Kết quả dịch thuật kèm thông tin bộ máy và ngôn ngữ
 */
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val engine: TranslationEngine,
    val sourceLang: String = "ko",
    val targetLang: String = "vi"
)

/**
 * Client kết nối dịch thuật Google Translate Unofficial (Free Web API)
 */
class GoogleTranslateClient {

    suspend fun translate(
        text: String,
        sourceLang: String = "ko",
        targetLang: String = "vi"
    ): Result<TranslationResult> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.success(
                TranslationResult(
                    originalText = text,
                    translatedText = "",
                    engine = TranslationEngine.GOOGLE_TRANSLATE,
                    sourceLang = sourceLang,
                    targetLang = targetLang
                )
            )
        }

        // 1. Thử endpoint clients5 (dict-chrome-ex) tốc độ cao & ổn định
        try {
            val encodedQuery = URLEncoder.encode(text.trim(), "UTF-8")
            val primaryUrl = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=$sourceLang&tl=$targetLang&q=$encodedQuery"
            val responseText = executeGetRequest(primaryUrl)
            val translated = parseChromeExResponse(responseText)
            if (translated.isNotBlank()) {
                return@withContext Result.success(
                    TranslationResult(
                        originalText = text,
                        translatedText = translated,
                        engine = TranslationEngine.GOOGLE_TRANSLATE,
                        sourceLang = sourceLang,
                        targetLang = targetLang
                    )
                )
            }
        } catch (_: Exception) {
            // Fallback sang endpoint tiếp theo
        }

        // 2. Fallback sang endpoint gtx
        try {
            val encodedQuery = URLEncoder.encode(text.trim(), "UTF-8")
            val fallbackUrl = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encodedQuery"
            val responseText = executeGetRequest(fallbackUrl)
            val translated = parseGtxResponse(responseText)
            return@withContext Result.success(
                TranslationResult(
                    originalText = text,
                    translatedText = translated,
                    engine = TranslationEngine.GOOGLE_TRANSLATE,
                    sourceLang = sourceLang,
                    targetLang = targetLang
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi Google Translate: ${e.message}", e))
        }
    }

    private fun executeGetRequest(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            conn.setRequestProperty("Accept", "*/*")

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP Error $responseCode")
            }

            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun parseChromeExResponse(jsonStr: String): String {
        val array = JSONArray(jsonStr)
        val sb = StringBuilder()
        for (i in 0 until array.length()) {
            val item = array.optString(i, "")
            if (item.isNotBlank()) {
                if (sb.isNotEmpty()) sb.append(" ")
                sb.append(item)
            }
        }
        return sb.toString().trim()
    }

    private fun parseGtxResponse(jsonStr: String): String {
        val array = JSONArray(jsonStr)
        val sentences = array.optJSONArray(0) ?: return ""
        val sb = StringBuilder()
        for (i in 0 until sentences.length()) {
            val part = sentences.optJSONArray(i)
            if (part != null) {
                val transSegment = part.optString(0, "")
                sb.append(transSegment)
            }
        }
        return sb.toString().trim()
    }
}

/**
 * Client kết nối DeepL Translation API (hỗ trợ cả API Free và DeepL Pro)
 */
class DeepLTranslateClient(
    private val apiKeyProvider: () -> String?
) {

    suspend fun translate(
        text: String,
        sourceLang: String = "KO",
        targetLang: String = "VI"
    ): Result<TranslationResult> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider()?.trim()
        if (key.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Chưa thiết lập DeepL API Key. Vui lòng vào Cài đặt > Quản lý API Key để thêm khóa.")
            )
        }

        if (text.isBlank()) {
            return@withContext Result.success(
                TranslationResult(
                    originalText = text,
                    translatedText = "",
                    engine = TranslationEngine.DEEPL,
                    sourceLang = sourceLang,
                    targetLang = targetLang
                )
            )
        }

        val baseUrl = if (key.endsWith(":fx", ignoreCase = true)) {
            "https://api-free.deepl.com/v2/translate"
        } else {
            "https://api.deepl.com/v2/translate"
        }

        val url = URL(baseUrl)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Authorization", "DeepL-Auth-Key $key")
            conn.setRequestProperty("User-Agent", "HanViet-EtymoAI/1.0")

            val jsonBody = JSONObject().apply {
                put("text", JSONArray().put(text.trim()))
                put("source_lang", sourceLang.uppercase())
                put("target_lang", targetLang.uppercase())
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream
                val errorText = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                return@withContext Result.failure(
                    IllegalStateException("DeepL API lỗi ($responseCode): $errorText")
                )
            }

            val responseBody = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(responseBody)
            val translations = root.optJSONArray("translations")
            if (translations != null && translations.length() > 0) {
                val first = translations.getJSONObject(0)
                val transText = first.optString("text", "")
                val detectedSource = first.optString("detected_source_language", sourceLang)
                Result.success(
                    TranslationResult(
                        originalText = text,
                        translatedText = transText,
                        engine = TranslationEngine.DEEPL,
                        sourceLang = detectedSource.lowercase(),
                        targetLang = targetLang.lowercase()
                    )
                )
            } else {
                Result.failure(IllegalStateException("DeepL không trả về kết quả dịch."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi DeepL Translate: ${e.message}", e))
        } finally {
            conn.disconnect()
        }
    }
}

/**
 * Dịch vụ Dịch thuật Hợp nhất (Unified Translation Service)
 * Điều phối dịch thuật giữa Google Translate, DeepL và AI Grounded
 */
class UnifiedTranslationService(
    private val apiKeyStorage: ApiKeyStorage,
    private val googleClient: GoogleTranslateClient = GoogleTranslateClient(),
    private val deepLClient: DeepLTranslateClient = DeepLTranslateClient { apiKeyStorage.getDeepLKey() }
) {

    /**
     * Dịch văn bản với bộ máy được cấu hình hoặc chỉ định trước
     */
    suspend fun translate(
        text: String,
        forcedEngine: TranslationEngine? = null,
        sourceLang: String = "ko",
        targetLang: String = "vi"
    ): Result<TranslationResult> {
        val activeEngine = forcedEngine ?: TranslationEngine.fromId(apiKeyStorage.getTranslationEngine())

        return when (activeEngine) {
            TranslationEngine.GOOGLE_TRANSLATE -> {
                googleClient.translate(text, sourceLang, targetLang)
            }
            TranslationEngine.DEEPL -> {
                deepLClient.translate(text, sourceLang, targetLang)
            }
            TranslationEngine.AI_GROUNDED -> {
                // Fallback nếu người dùng gọi hàm máy dịch với engine AI
                googleClient.translate(text, sourceLang, targetLang)
            }
        }
    }

    /**
     * Lấy bộ máy dịch thuật đang được chọn làm mặc định
     */
    fun getActiveEngine(): TranslationEngine {
        return TranslationEngine.fromId(apiKeyStorage.getTranslationEngine())
    }

    /**
     * Lưu bộ máy dịch thuật mặc định
     */
    fun setActiveEngine(engine: TranslationEngine) {
        apiKeyStorage.saveTranslationEngine(engine.id)
    }
}
