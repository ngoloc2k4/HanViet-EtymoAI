package com.hanviet.etymoai.ai.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client kết nối Google Gemini Developer REST API (Gemini 1.5 Flash / Pro)
 * Hỗ trợ native systemInstruction và JSON Schema response enforcement.
 */
class GeminiClient(
    override val providerType: AiProviderType = AiProviderType.GEMINI,
    private val apiKeyProvider: () -> String?,
    private val baseUrl: String = providerType.defaultBaseUrl,
    private val modelName: String = providerType.defaultModel,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val timeoutMillis: Int = 15000,
    private val httpTransport: ((url: String, method: String, headers: Map<String, String>, body: String?) -> Pair<Int, String>)? = null
) : AiProvider {

    override suspend fun explainEtymology(prompt: GroundedPrompt): Result<AiEtymologyExplanation> = withContext(dispatcher) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Chưa thiết lập Google Gemini API Key."))
        }

        val systemPrompt = """
            Bạn là chuyên gia ngôn ngữ học Hán-Nôm và Tiếng Hàn của ứng dụng HanViet-EtymoAI.
            QUY TẮC BẮT BUỘC (GROUNDING GUARD):
            1. Bạn CHỈ ĐƯỢC PHÉP giải thích dựa trên các chữ Hán và dữ liệu từ nguyên được cung cấp trong Grounding Context.
            2. TUYỆT ĐỐI KHÔNG tự sáng tác thêm chữ Hán mới nếu từ là thuần Hàn (NATIVE_KOREAN) hoặc không có trong dữ liệu neo.
            3. Phản hồi BẮT BUỘC bằng định dạng JSON thuần túy theo schema:
            {
               "etymologySummaryVi": "Tóm tắt từ nguyên ngắn gọn (1-2 câu)",
               "deepNuanceExplanationVi": "Giải thích chiết tự và chuyển nghĩa văn hóa sâu sắc",
               "culturalContextVi": "Bối cảnh văn hóa / lịch sử Hán-Hàn (nếu có)",
               "mnemonicsTipVi": "Mẹo ghi nhớ liên tưởng qua âm Hán Việt"
            }
        """.trimIndent()

        val userMessage = buildEtymologyUserMessage(prompt)

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        val endpoint = "${baseUrl.trimEnd('/')}/models/$modelName:generateContent?key=$apiKey"
        val headers = mapOf("Content-Type" to "application/json")

        try {
            val (statusCode, responseBody) = executeHttpRequest(endpoint, "POST", headers, requestJson.toString())
            if (statusCode !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("Lỗi từ Google Gemini ($statusCode): $responseBody")
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(IllegalStateException("Gemini không trả về kết quả (candidates rỗng)."))
            }

            val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            val textContent = parts.getJSONObject(0).getString("text")
            val parsed = JSONObject(cleanJsonWrapper(textContent))

            val explanation = AiEtymologyExplanation(
                headword = prompt.headword,
                etymologySummaryVi = parsed.optString("etymologySummaryVi", "Không có tóm tắt."),
                deepNuanceExplanationVi = parsed.optString("deepNuanceExplanationVi", "Không có giải thích chi tiết."),
                culturalContextVi = parsed.optString("culturalContextVi").takeIf { it.isNotBlank() },
                mnemonicsTipVi = parsed.optString("mnemonicsTipVi").takeIf { it.isNotBlank() },
                rawJson = textContent
            )

            Result.success(explanation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun compareSynonyms(prompt: GroundedPrompt): Result<AiSynonymComparison> = withContext(dispatcher) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Chưa thiết lập Google Gemini API Key."))
        }

        val targetWord = prompt.targetSynonymHeadword ?: return@withContext Result.failure(
            IllegalArgumentException("Cần chỉ định targetSynonymHeadword để so sánh từ đồng nghĩa.")
        )

        val systemPrompt = """
            Bạn là chuyên gia ngữ nghĩa học Tiếng Hàn.
            Nhiệm vụ: So sánh sắc thái và ngữ cảnh sử dụng giữa 2 từ: '${prompt.headword}' và '$targetWord'.
            Phản hồi BẮT BUỘC bằng JSON thuần túy:
            {
               "differenceVi": "Sự khác biệt cốt lõi về sắc thái biểu cảm hoặc mức độ trang trọng",
               "usageNotesVi": "Khi nào nên dùng từ này thay vì từ kia",
               "exampleSentenceA": "Câu ví dụ minh họa bằng tiếng Hàn kèm dịch nghĩa tiếng Việt cho từ '${prompt.headword}'",
               "exampleSentenceB": "Câu ví dụ minh họa bằng tiếng Hàn kèm dịch nghĩa tiếng Việt cho từ '$targetWord'"
            }
        """.trimIndent()

        val userMessage = "So sánh sắc thái của 2 từ: '${prompt.headword}' (${prompt.primaryMeaning}) và '$targetWord' (${prompt.targetSynonymMeaning ?: ""})."

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val endpoint = "${baseUrl.trimEnd('/')}/models/$modelName:generateContent?key=$apiKey"
        val headers = mapOf("Content-Type" to "application/json")

        try {
            val (statusCode, responseBody) = executeHttpRequest(endpoint, "POST", headers, requestJson.toString())
            if (statusCode !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("Lỗi từ Google Gemini ($statusCode): $responseBody")
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.getJSONArray("candidates")
            val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            val textContent = parts.getJSONObject(0).getString("text")
            val parsed = JSONObject(cleanJsonWrapper(textContent))

            val comparison = AiSynonymComparison(
                wordA = prompt.headword,
                wordB = targetWord,
                differenceVi = parsed.optString("differenceVi", "Không có thông tin khác biệt."),
                usageNotesVi = parsed.optString("usageNotesVi", "Không có lưu ý sử dụng."),
                exampleSentenceA = parsed.optString("exampleSentenceA").takeIf { it.isNotBlank() },
                exampleSentenceB = parsed.optString("exampleSentenceB").takeIf { it.isNotBlank() },
                rawJson = textContent
            )

            Result.success(comparison)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildEtymologyUserMessage(prompt: GroundedPrompt): String {
        val sb = StringBuilder()
        sb.append("Từ cần giải thích: ").append(prompt.headword).append("\n")
        sb.append("Nghĩa trong từ điển: ").append(prompt.primaryMeaning).append("\n")
        sb.append("Nguồn gốc (OriginType): ").append(prompt.originType).append("\n")

        if (prompt.hanjaSegments.isNotEmpty()) {
            sb.append("Các phân đoạn Hán tự đã kiểm định (Grounding Segments):\n")
            for (seg in prompt.hanjaSegments) {
                sb.append(" - Âm tiết [").append(seg.syllable).append("] -> ")
                if (seg.hanjaChar != null) {
                    sb.append("Chữ Hán: ").append(seg.hanjaChar)
                        .append(", Âm Hán-Việt: ").append(seg.readingVi ?: "?")
                        .append(", Nghĩa Hán tự: ").append(seg.meaningVi ?: "?")
                } else {
                    sb.append("Không có chữ Hán (Thuần Hàn)")
                }
                sb.append("\n")
            }
        } else {
            sb.append("Không có phân đoạn Hán tự (Từ thuần Hàn hoặc ngoại lai).\n")
        }

        return sb.toString()
    }

    private fun cleanJsonWrapper(content: String): String {
        var str = content.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun executeHttpRequest(
        urlStr: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): Pair<Int, String> {
        httpTransport?.let { transport ->
            return transport(urlStr, method, headers, body)
        }

        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = timeoutMillis
        conn.readTimeout = timeoutMillis
        conn.doInput = true

        headers.forEach { (k, v) ->
            conn.setRequestProperty(k, v)
        }

        if (body != null) {
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }
        }

        val statusCode = conn.responseCode
        val stream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
        val responseBody = if (stream != null) {
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        } else {
            ""
        }
        conn.disconnect()
        return Pair(statusCode, responseBody)
    }
}
