package com.hanviet.etymoai.ai.grounding

import com.hanviet.etymoai.ai.provider.AiQueryType
import com.hanviet.etymoai.ai.provider.GroundedPrompt
import com.hanviet.etymoai.ai.provider.HanjaSegmentPromptInfo
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.WordEtymology
import java.security.MessageDigest

/**
 * Bộ tạo câu lệnh có cấu trúc (Etymology Prompt Builder)
 * Đảm bảo 100% dữ liệu từ điển thực tế từ database lõi được tiêm vào trước khi gửi tới LLM.
 */
object EtymologyPromptBuilder {

    /**
     * Chuyển đổi WordEtymology thành GroundedPrompt chuẩn hóa
     */
    fun buildEtymologyPrompt(
        etymology: WordEtymology,
        primaryMeaning: String
    ): GroundedPrompt {
        val segmentInfos = etymology.segments.map { seg ->
            HanjaSegmentPromptInfo(
                syllable = seg.syllableKo,
                hanjaChar = seg.hanjaChar,
                readingVi = seg.readingVi,
                meaningVi = seg.meaningVi
            )
        }

        return GroundedPrompt(
            headword = etymology.headword,
            primaryMeaning = primaryMeaning,
            originType = etymology.originType.name,
            hanjaSegments = segmentInfos,
            queryType = AiQueryType.ETYMOLOGY_EXPLANATION
        )
    }

    /**
     * Xây dựng câu lệnh so sánh từ đồng nghĩa có neo dữ liệu
     */
    fun buildSynonymComparisonPrompt(
        wordA: WordEtymology,
        meaningA: String,
        wordB: WordEtymology,
        meaningB: String
    ): GroundedPrompt {
        val segmentInfos = wordA.segments.map { seg ->
            HanjaSegmentPromptInfo(
                syllable = seg.syllableKo,
                hanjaChar = seg.hanjaChar,
                readingVi = seg.readingVi,
                meaningVi = seg.meaningVi
            )
        }

        return GroundedPrompt(
            headword = wordA.headword,
            primaryMeaning = meaningA,
            originType = wordA.originType.name,
            hanjaSegments = segmentInfos,
            queryType = AiQueryType.SYNONYM_COMPARISON,
            targetSynonymHeadword = wordB.headword,
            targetSynonymMeaning = meaningB
        )
    }

    /**
     * Tính toán khóa băm SHA-256 duy nhất cho prompt phục vụ bộ nhớ đệm (AiCache)
     */
    fun computePromptHash(prompt: GroundedPrompt, modelId: String): String {
        val raw = buildString {
            append(prompt.queryType.name).append(":")
            append(prompt.headword).append(":")
            append(prompt.originType).append(":")
            append(modelId).append(":")
            prompt.hanjaSegments.forEach {
                append(it.syllable).append("=").append(it.hanjaChar ?: "").append(";")
            }
            prompt.targetSynonymHeadword?.let {
                append("vs=").append(it)
            }
        }

        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
