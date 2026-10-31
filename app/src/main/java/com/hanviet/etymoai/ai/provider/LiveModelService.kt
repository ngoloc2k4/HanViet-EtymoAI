package com.hanviet.etymoai.ai.provider

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class LiveModelInfo(
    val id: String,
    val displayName: String = id,
    val description: String = ""
)

/**
 * LiveModelService: Tải danh sách model trực tiếp từ HTTP GET endpoint của từng nhà cung cấp AI
 * và lưu bộ nhớ đệm (Cache) trong vòng 7 ngày theo đúng yêu cầu người dùng.
 */
class LiveModelService(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Lấy danh sách models từ Cache nếu chưa quá 7 ngày.
     */
    fun getCachedModels(provider: AiProviderType): List<LiveModelInfo>? {
        val lastFetched = prefs.getLong(getKeyTimestamp(provider), 0L)
        val now = System.currentTimeMillis()
        if (now - lastFetched > CACHE_DURATION_MILLIS) {
            return null
        }
        val jsonStr = prefs.getString(getKeyJson(provider), null) ?: return null
        return parseModelsJson(jsonStr)
    }

    /**
     * Chuỗi hiển thị tuổi của cache (ví dụ: "Đã lưu đệm 2 ngày trước • Hiệu lực 7 ngày")
     */
    fun getCacheAgeText(provider: AiProviderType): String? {
        val lastFetched = prefs.getLong(getKeyTimestamp(provider), 0L)
        if (lastFetched <= 0L) return null
        val diffDays = (System.currentTimeMillis() - lastFetched) / (24 * 60 * 60 * 1000L)
        val remainingDays = (7 - diffDays).coerceAtLeast(1)
        return "Đã lưu đệm $diffDays ngày trước (còn hiệu lực $remainingDays ngày)"
    }

    /**
     * Thực hiện HTTP GET đến API Endpoint của nhà cung cấp để tải danh sách model trực tiếp và lưu cache 7 ngày.
     */
    suspend fun fetchLiveModels(
        provider: AiProviderType,
        apiKey: String,
        customEndpoint: String? = null
    ): Result<List<LiveModelInfo>> = withContext(Dispatchers.IO) {
        try {
            val endpointUrl = buildModelsUrl(provider, apiKey, customEndpoint)
            val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Accept", "application/json")
                when (provider) {
                    AiProviderType.OPENAI, AiProviderType.DEEPSEEK -> {
                        if (apiKey.isNotBlank()) setRequestProperty("Authorization", "Bearer $apiKey")
                    }
                    AiProviderType.CLAUDE -> {
                        if (apiKey.isNotBlank()) {
                            setRequestProperty("x-api-key", apiKey)
                            setRequestProperty("anthropic-version", "2023-06-01")
                        }
                    }
                    AiProviderType.GEMINI -> {
                        if (apiKey.isNotBlank()) setRequestProperty("x-goog-api-key", apiKey)
                    }
                    AiProviderType.LOCAL_GGUF -> {}
                }
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val rawResponse = reader.use { it.readText() }
                val models = parseProviderResponse(provider, rawResponse)
                if (models.isNotEmpty()) {
                    saveModelsCache(provider, models)
                    Result.success(models)
                } else {
                    Result.failure(Exception("Phản hồi hợp lệ nhưng không tìm thấy danh sách model khả dụng."))
                }
            } else {
                val errReader = connection.errorStream?.let { BufferedReader(InputStreamReader(it, "UTF-8")) }
                val errBody = errReader?.use { it.readText() } ?: ""
                Log.w(TAG, "Lỗi HTTP $responseCode khi tải models từ ${provider.name}: $errBody")
                Result.failure(Exception("HTTP $responseCode: ${extractErrorMessage(errBody)}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ngoại lệ khi gọi GET models cho ${provider.name}", e)
            Result.failure(e)
        }
    }

    private fun buildModelsUrl(
        provider: AiProviderType,
        apiKey: String,
        customEndpoint: String?
    ): String {
        val base = customEndpoint?.takeIf { it.isNotBlank() } ?: provider.defaultBaseUrl
        val cleanBase = base.trimEnd('/')

        return when (provider) {
            AiProviderType.GEMINI -> {
                if (cleanBase.contains("generativelanguage.googleapis.com")) {
                    "https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey.trim()}"
                } else {
                    "$cleanBase/models"
                }
            }
            AiProviderType.OPENAI -> {
                if (cleanBase.endsWith("/v1")) "$cleanBase/models" else "$cleanBase/v1/models"
            }
            AiProviderType.DEEPSEEK -> {
                if (cleanBase.endsWith("/v1")) "$cleanBase/models" else "$cleanBase/models"
            }
            AiProviderType.CLAUDE -> {
                if (cleanBase.endsWith("/v1")) "$cleanBase/models" else "$cleanBase/v1/models"
            }
            AiProviderType.LOCAL_GGUF -> {
                if (cleanBase.contains(":11434")) "$cleanBase/api/tags" else "$cleanBase/models"
            }
        }
    }

    private fun parseProviderResponse(provider: AiProviderType, jsonString: String): List<LiveModelInfo> {
        val list = mutableListOf<LiveModelInfo>()
        val root = JSONObject(jsonString)

        when (provider) {
            AiProviderType.GEMINI -> {
                val array = root.optJSONArray("models") ?: JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val rawName = obj.optString("name", "")
                    val id = rawName.removePrefix("models/")
                    val methods = obj.optJSONArray("supportedGenerationMethods")
                    var supportsGenerate = false
                    if (methods != null) {
                        for (m in 0 until methods.length()) {
                            if (methods.getString(m) == "generateContent") {
                                supportsGenerate = true
                                break
                            }
                        }
                    }
                    if (supportsGenerate && id.contains("gemini", ignoreCase = true)) {
                        list.add(
                            LiveModelInfo(
                                id = id,
                                displayName = obj.optString("displayName", id),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                }
            }
            AiProviderType.OPENAI, AiProviderType.DEEPSEEK, AiProviderType.CLAUDE -> {
                val array = root.optJSONArray("data") ?: JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", "")
                    if (id.isNotBlank()) {
                        val name = obj.optString("display_name", id)
                        list.add(LiveModelInfo(id = id, displayName = name))
                    }
                }
            }
            AiProviderType.LOCAL_GGUF -> {
                val array = root.optJSONArray("models") ?: JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.optString("name", "")
                    if (name.isNotBlank()) {
                        list.add(LiveModelInfo(id = name, displayName = name))
                    }
                }
            }
        }

        // Ưu tiên hiển thị các model phổ biến lên đầu
        return list.sortedWith(
            compareByDescending<LiveModelInfo> { m ->
                val id = m.id.lowercase()
                when {
                    id.contains("2.5-flash") || id.contains("3-7-sonnet") || id.contains("gpt-4o") || id.contains("reasoner") -> 4
                    id.contains("2.0-flash") || id.contains("3-5-sonnet") || id.contains("4o-mini") || id.contains("deepseek-chat") -> 3
                    id.contains("1.5-pro") || id.contains("3-5-haiku") -> 2
                    else -> 1
                }
            }.thenBy { it.id }
        )
    }

    private fun saveModelsCache(provider: AiProviderType, models: List<LiveModelInfo>) {
        val array = JSONArray()
        for (m in models) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("displayName", m.displayName)
            obj.put("description", m.description)
            array.put(obj)
        }
        prefs.edit()
            .putString(getKeyJson(provider), array.toString())
            .putLong(getKeyTimestamp(provider), System.currentTimeMillis())
            .apply()
    }

    private fun parseModelsJson(jsonStr: String): List<LiveModelInfo> {
        val list = mutableListOf<LiveModelInfo>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                LiveModelInfo(
                    id = obj.getString("id"),
                    displayName = obj.optString("displayName", obj.getString("id")),
                    description = obj.optString("description", "")
                )
            )
        }
        return list
    }

    private fun extractErrorMessage(errBody: String): String {
        return try {
            val obj = JSONObject(errBody)
            val err = obj.optJSONObject("error")
            err?.optString("message") ?: errBody.take(120)
        } catch (_: Exception) {
            errBody.take(120)
        }
    }

    private fun getKeyJson(provider: AiProviderType) = "cache_models_json_${provider.name}"
    private fun getKeyTimestamp(provider: AiProviderType) = "cache_models_time_${provider.name}"

    companion object {
        private const val TAG = "LiveModelService"
        private const val PREFS_NAME = "live_models_cache"
        private const val CACHE_DURATION_MILLIS = 7 * 24 * 60 * 60 * 1000L // 7 ngày
    }
}
