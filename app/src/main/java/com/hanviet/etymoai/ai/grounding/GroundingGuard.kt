package com.hanviet.etymoai.ai.grounding

import com.hanviet.etymoai.ai.provider.AiEtymologyExplanation
import com.hanviet.etymoai.ai.provider.AiProvider
import com.hanviet.etymoai.ai.provider.AiSynonymComparison
import com.hanviet.etymoai.ai.provider.GroundedPrompt
import com.hanviet.etymoai.data.db.workspace.dao.AiCacheDao
import com.hanviet.etymoai.data.db.workspace.entity.AiCacheEntity
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Bộ Lọc Neo Dữ Liệu & Chống Ảo Giác AI (Local Grounding Guard)
 * Thực thi nguyên tắc cốt lõi: "Không bao giờ bịa nguồn gốc".
 */
class GroundingGuard(
    private val aiCacheDao: AiCacheDao? = null
) {

    companion object {
        // Biểu thức chính quy phát hiện chữ Hán (CJK Unified Ideographs)
        private val HANJA_REGEX = Pattern.compile("[\\u4E00-\\u9FFF\\u3400-\\u4DBF\\uF900-\\uFAFF]")

        /**
         * Trích xuất tập hợp tất cả các chữ Hán xuất hiện trong chuỗi văn bản
         */
        fun extractHanjaCharacters(text: String): Set<String> {
            val set = mutableSetOf<String>()
            val matcher = HANJA_REGEX.matcher(text)
            while (matcher.find()) {
                set.add(matcher.group())
            }
            return set
        }
    }

    /**
     * Kiểm duyệt và phát hiện ảo giác Hán tự trong phản hồi từ AI
     */
    fun auditExplanation(
        rawExplanation: AiEtymologyExplanation,
        prompt: GroundedPrompt
    ): AiEtymologyExplanation {
        val validHanjaSet = prompt.hanjaSegments.mapNotNull { it.hanjaChar?.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { extractHanjaCharacters(it) }
            .toSet()

        // Trích xuất toàn bộ chữ Hán mà AI đã tự đưa vào câu trả lời
        val fullAiText = "${rawExplanation.etymologySummaryVi} ${rawExplanation.deepNuanceExplanationVi} ${rawExplanation.culturalContextVi ?: ""} ${rawExplanation.mnemonicsTipVi ?: ""}"
        val foundHanjaInAi = extractHanjaCharacters(fullAiText)

        var hasHallucination = false

        if (prompt.originType == "NATIVE_KOREAN") {
            // Đối với từ thuần Hàn, nếu AI cố tình gán ghép chữ Hán làm từ nguyên -> Đánh cờ ảo giác
            if (foundHanjaInAi.isNotEmpty()) {
                hasHallucination = true
            }
        } else if (prompt.originType == "SINO_KOREAN") {
            // Đối với từ Hán-Hàn, các chữ Hán xuất hiện phải nằm trong tập hợp các chữ Hán đã xác thực
            val unverifiedHanja = foundHanjaInAi.filter { it !in validHanjaSet }
            if (unverifiedHanja.isNotEmpty()) {
                hasHallucination = true
            }
        }

        return rawExplanation.copy(hasUnverifiedHanja = hasHallucination)
    }

    /**
     * Thực hiện gọi AI có kèm bộ nhớ đệm offline O(1) và bảo vệ Grounding
     */
    suspend fun executeGroundedEtymology(
        provider: AiProvider,
        prompt: GroundedPrompt,
        modelId: String = provider.providerType.defaultModel
    ): Result<AiEtymologyExplanation> {
        val promptHash = EtymologyPromptBuilder.computePromptHash(prompt, modelId)

        // 1. Kiểm tra cache trong Workspace DB
        if (aiCacheDao != null) {
            try {
                val cached = aiCacheDao.getByHash(promptHash)
                if (cached != null) {
                    val parsed = deserializeExplanation(cached.responseJson, prompt.headword)
                    if (parsed != null) {
                        return Result.success(parsed)
                    }
                }
            } catch (_: Exception) {
                // Tiếp tục gọi online nếu đọc cache gặp lỗi
            }
        }

        // 2. Gọi provider
        val responseResult = provider.explainEtymology(prompt)
        if (responseResult.isFailure) {
            return responseResult
        }

        val explanation = responseResult.getOrThrow()

        // 3. Kiểm định Grounding Guard chống ảo giác
        val audited = auditExplanation(explanation, prompt)

        // 4. Lưu cache nếu hợp lệ
        if (aiCacheDao != null && !audited.hasUnverifiedHanja) {
            try {
                val serialized = serializeExplanation(audited)
                aiCacheDao.insert(
                    AiCacheEntity(
                        promptHash = promptHash,
                        responseJson = serialized,
                        modelId = modelId
                    )
                )
            } catch (_: Exception) {
                // Lỗi ghi cache không làm gián đoạn luồng chính
            }
        }

        return Result.success(audited)
    }

    private fun serializeExplanation(exp: AiEtymologyExplanation): String {
        return JSONObject().apply {
            put("headword", exp.headword)
            put("etymologySummaryVi", exp.etymologySummaryVi)
            put("deepNuanceExplanationVi", exp.deepNuanceExplanationVi)
            put("culturalContextVi", exp.culturalContextVi ?: "")
            put("mnemonicsTipVi", exp.mnemonicsTipVi ?: "")
            put("hasUnverifiedHanja", exp.hasUnverifiedHanja)
            put("rawJson", exp.rawJson ?: "")
        }.toString()
    }

    private fun deserializeExplanation(jsonStr: String, fallbackHeadword: String): AiEtymologyExplanation? {
        return try {
            val json = JSONObject(jsonStr)
            AiEtymologyExplanation(
                headword = json.optString("headword", fallbackHeadword),
                etymologySummaryVi = json.getString("etymologySummaryVi"),
                deepNuanceExplanationVi = json.getString("deepNuanceExplanationVi"),
                culturalContextVi = json.optString("culturalContextVi").takeIf { it.isNotBlank() },
                mnemonicsTipVi = json.optString("mnemonicsTipVi").takeIf { it.isNotBlank() },
                hasUnverifiedHanja = json.optBoolean("hasUnverifiedHanja", false),
                rawJson = json.optString("rawJson")
            )
        } catch (_: Exception) {
            null
        }
    }
}
