package com.hanviet.etymoai.ai.provider

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiProviderClientTest {

    @Test
    fun testOpenAiCompatibleClient_explainEtymology_success() = runBlocking {
        val mockResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\"etymologySummaryVi\":\"Từ Hán-Hàn xuất phát từ Khí (氣) và Phân (分).\",\"deepNuanceExplanationVi\":\"Khí biểu thị hơi thở, năng lượng tâm lý; Phân biểu thị sự phân định, trạng thái. Hợp lại chỉ tâm trạng con người.\",\"culturalContextVi\":\"Xuất hiện trong y học cổ truyền và văn học cổ điển Á Đông.\",\"mnemonicsTipVi\":\"Nhớ qua 'khí sắc' và 'phần định'.\"}"
                  }
                }
              ]
            }
        """.trimIndent()

        val client = OpenAiCompatibleClient(
            providerType = AiProviderType.OPENAI,
            apiKeyProvider = { "mock-key" },
            httpTransport = { url, method, headers, body ->
                assertTrue(url.contains("/chat/completions"))
                assertEquals("POST", method)
                assertEquals("Bearer mock-key", headers["Authorization"])
                assertNotNull(body)
                assertTrue(body!!.contains("기분"))
                Pair(200, mockResponse)
            }
        )

        val prompt = GroundedPrompt(
            headword = "기분",
            primaryMeaning = "Tâm trạng, cảm giác",
            originType = "SINO_KOREAN",
            hanjaSegments = listOf(
                HanjaSegmentPromptInfo("기", "氣", "khí", "hơi, năng lượng"),
                HanjaSegmentPromptInfo("분", "分", "phân", "chia, phần")
            )
        )

        val result = client.explainEtymology(prompt)
        assertTrue(result.isSuccess)

        val explanation = result.getOrNull()
        assertNotNull(explanation)
        assertEquals("기분", explanation!!.headword)
        assertTrue(explanation.etymologySummaryVi.contains("Khí (氣)"))
        assertTrue(explanation.deepNuanceExplanationVi.contains("hơi thở"))
        assertEquals("Xuất hiện trong y học cổ truyền và văn học cổ điển Á Đông.", explanation.culturalContextVi)
        assertEquals("Nhớ qua 'khí sắc' và 'phần định'.", explanation.mnemonicsTipVi)
    }

    @Test
    fun testOpenAiCompatibleClient_compareSynonyms_success() = runBlocking {
        val mockResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\"differenceVi\":\"'기분' thiên về cảm xúc ngắn hạn tức thời, còn '감정' là trạng thái tình cảm sâu sắc, có tính ổn định hơn.\",\"usageNotesVi\":\"Dùng '기분' khi nói vui buồn trong ngày; dùng '감정' khi nói về tình yêu, thù hận, cảm xúc đạo đức.\",\"exampleSentenceA\":\"오늘 기분이 좋아요 (Hôm nay tâm trạng tôi rất tốt).\",\"exampleSentenceB\":\"감정을 억제하기 어려워요 (Thật khó để kiềm chế cảm xúc).\"}"
                  }
                }
              ]
            }
        """.trimIndent()

        val client = OpenAiCompatibleClient(
            providerType = AiProviderType.DEEPSEEK,
            apiKeyProvider = { "deepseek-test-key" },
            httpTransport = { _, _, _, _ ->
                Pair(200, mockResponse)
            }
        )

        val prompt = GroundedPrompt(
            headword = "기분",
            primaryMeaning = "Tâm trạng",
            originType = "SINO_KOREAN",
            queryType = AiQueryType.SYNONYM_COMPARISON,
            targetSynonymHeadword = "감정",
            targetSynonymMeaning = "Cảm xúc, tình cảm"
        )

        val result = client.compareSynonyms(prompt)
        assertTrue(result.isSuccess)

        val comparison = result.getOrNull()
        assertNotNull(comparison)
        assertEquals("기분", comparison!!.wordA)
        assertEquals("감정", comparison.wordB)
        assertTrue(comparison.differenceVi.contains("ngắn hạn tức thời"))
        assertNotNull(comparison.exampleSentenceA)
        assertNotNull(comparison.exampleSentenceB)
    }

    @Test
    fun testOpenAiCompatibleClient_missingApiKey_returnsFailure() = runBlocking {
        val client = OpenAiCompatibleClient(
            providerType = AiProviderType.OPENAI,
            apiKeyProvider = { null }
        )

        val prompt = GroundedPrompt(headword = "학교", primaryMeaning = "Trường học", originType = "SINO_KOREAN")
        val result = client.explainEtymology(prompt)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Chưa thiết lập API Key"))
    }

    @Test
    fun testGeminiClient_explainEtymology_success() = runBlocking {
        val mockResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\"etymologySummaryVi\":\"Học sinh (學生) là người theo học.\",\"deepNuanceExplanationVi\":\"Học (學) là học hỏi tiếp thu tri thức; Sinh (生) là người sống, môn sinh. Thuật ngữ Nho giáo chỉ thế hệ trẻ thụ giáo.\",\"culturalContextVi\":\"Nền giáo dục Nho học khoa bảng triều Joseon.\",\"mnemonicsTipVi\":\"Âm Hán Việt tương đương 100% tiếng Việt: Học Sinh.\"}"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val client = GeminiClient(
            apiKeyProvider = { "gemini-mock-key" },
            httpTransport = { url, method, _, body ->
                assertTrue(url.contains("generativelanguage.googleapis.com"))
                assertTrue(url.contains("key=gemini-mock-key"))
                assertEquals("POST", method)
                assertNotNull(body)
                assertTrue(body!!.contains("generationConfig"))
                Pair(200, mockResponse)
            }
        )

        val prompt = GroundedPrompt(
            headword = "학생",
            primaryMeaning = "Học sinh",
            originType = "SINO_KOREAN",
            hanjaSegments = listOf(
                HanjaSegmentPromptInfo("학", "學", "học", "học tập"),
                HanjaSegmentPromptInfo("생", "生", "sinh", "sống, sinh sôi")
            )
        )

        val result = client.explainEtymology(prompt)
        assertTrue(result.isSuccess)

        val explanation = result.getOrNull()
        assertNotNull(explanation)
        assertEquals("학생", explanation!!.headword)
        assertTrue(explanation.etymologySummaryVi.contains("Học sinh (學生)"))
        assertTrue(explanation.deepNuanceExplanationVi.contains("tri thức"))
        assertEquals("Nền giáo dục Nho học khoa bảng triều Joseon.", explanation.culturalContextVi)
    }

    @Test
    fun testGeminiClient_httpError_returnsFailure() = runBlocking {
        val client = GeminiClient(
            apiKeyProvider = { "bad-key" },
            httpTransport = { _, _, _, _ ->
                Pair(403, "{\"error\": {\"message\": \"API_KEY_INVALID\"}}")
            }
        )

        val prompt = GroundedPrompt(headword = "선생", primaryMeaning = "Tiên sinh", originType = "SINO_KOREAN")
        val result = client.explainEtymology(prompt)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("403"))
    }
}
