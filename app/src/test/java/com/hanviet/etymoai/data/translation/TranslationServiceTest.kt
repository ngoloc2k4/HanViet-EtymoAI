package com.hanviet.etymoai.data.translation

import com.hanviet.etymoai.ai.provider.AiProviderType
import com.hanviet.etymoai.ai.security.ApiKeyStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TranslationServiceTest {

    private class FakeApiKeyStorage : ApiKeyStorage {
        private var deepLKey: String? = null
        private var translationEngine: String = "GOOGLE_TRANSLATE"

        override fun saveApiKey(provider: AiProviderType, key: String) {}
        override fun getApiKey(provider: AiProviderType): String? = null
        override fun clearApiKey(provider: AiProviderType) {}
        override fun hasApiKey(provider: AiProviderType): Boolean = false
        override fun clearAllKeys() {}
        override fun saveEndpoint(provider: AiProviderType, endpoint: String) {}
        override fun getEndpoint(provider: AiProviderType): String = provider.defaultBaseUrl
        override fun saveModel(provider: AiProviderType, model: String) {}
        override fun getModel(provider: AiProviderType): String = provider.defaultModel

        override fun saveDeepLKey(key: String) { deepLKey = key }
        override fun getDeepLKey(): String? = deepLKey
        override fun clearDeepLKey() { deepLKey = null }
        override fun hasDeepLKey(): Boolean = !deepLKey.isNullOrBlank()

        override fun saveTranslationEngine(engineName: String) { translationEngine = engineName }
        override fun getTranslationEngine(): String = translationEngine
    }

    private lateinit var fakeStorage: FakeApiKeyStorage
    private lateinit var translationService: UnifiedTranslationService

    @Before
    fun setUp() {
        fakeStorage = FakeApiKeyStorage()
        translationService = UnifiedTranslationService(fakeStorage)
    }

    @Test
    fun testTranslationEngineEnum() {
        assertEquals(TranslationEngine.GOOGLE_TRANSLATE, TranslationEngine.fromId("GOOGLE_TRANSLATE"))
        assertEquals(TranslationEngine.DEEPL, TranslationEngine.fromId("DEEPL"))
        assertEquals(TranslationEngine.AI_GROUNDED, TranslationEngine.fromId("AI_GROUNDED"))
        // Fallback default
        assertEquals(TranslationEngine.GOOGLE_TRANSLATE, TranslationEngine.fromId("UNKNOWN_ENGINE"))
        assertEquals(TranslationEngine.GOOGLE_TRANSLATE, TranslationEngine.fromId(null))
    }

    @Test
    fun testDefaultActiveEngine() {
        assertEquals(TranslationEngine.GOOGLE_TRANSLATE, translationService.getActiveEngine())

        translationService.setActiveEngine(TranslationEngine.DEEPL)
        assertEquals(TranslationEngine.DEEPL, translationService.getActiveEngine())
    }

    @Test
    fun testDeepLClientFailsWithoutApiKey() = runBlocking {
        val client = DeepLTranslateClient { fakeStorage.getDeepLKey() }
        val result = client.translate("안녕하세요")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Chưa thiết lập DeepL API Key") == true)
    }

    @Test
    fun testDeepLClientHandlesBlankInput() = runBlocking {
        fakeStorage.saveDeepLKey("fake-key:fx")
        val client = DeepLTranslateClient { fakeStorage.getDeepLKey() }
        val result = client.translate("   ")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull()?.translatedText)
    }
}
