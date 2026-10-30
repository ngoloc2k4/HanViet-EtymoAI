package com.hanviet.etymoai.ai.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.ai.provider.AiProviderType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApiKeyStorageTest {

    private lateinit var storage: SecureApiKeyStorage

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test_byok_keys", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // 256-bit test AES key để kiểm thử tính mã hóa
        val testKey = ByteArray(32) { (it + 1).toByte() }
        storage = SecureApiKeyStorage(prefs, testKey)
    }

    @Test
    fun testSaveAndRetrieveApiKey() {
        assertFalse(storage.hasApiKey(AiProviderType.OPENAI))
        assertNull(storage.getApiKey(AiProviderType.OPENAI))

        val fakeKey = "sk-proj-test1234567890abcdef"
        storage.saveApiKey(AiProviderType.OPENAI, fakeKey)

        assertTrue(storage.hasApiKey(AiProviderType.OPENAI))
        assertEquals(fakeKey, storage.getApiKey(AiProviderType.OPENAI))
    }

    @Test
    fun testMultipleProvidersIsolation() {
        val openAiKey = "sk-openai-key-999"
        val geminiKey = "AIzaSyGemini-key-888"
        val deepSeekKey = "sk-deepseek-key-777"

        storage.saveApiKey(AiProviderType.OPENAI, openAiKey)
        storage.saveApiKey(AiProviderType.GEMINI, geminiKey)
        storage.saveApiKey(AiProviderType.DEEPSEEK, deepSeekKey)

        assertEquals(openAiKey, storage.getApiKey(AiProviderType.OPENAI))
        assertEquals(geminiKey, storage.getApiKey(AiProviderType.GEMINI))
        assertEquals(deepSeekKey, storage.getApiKey(AiProviderType.DEEPSEEK))

        // Provider không cần API key luôn trả về true cho hasApiKey
        assertTrue(storage.hasApiKey(AiProviderType.LOCAL_GGUF))
    }

    @Test
    fun testClearApiKey() {
        storage.saveApiKey(AiProviderType.CLAUDE, "sk-ant-claude-key")
        assertTrue(storage.hasApiKey(AiProviderType.CLAUDE))

        storage.clearApiKey(AiProviderType.CLAUDE)
        assertFalse(storage.hasApiKey(AiProviderType.CLAUDE))
        assertNull(storage.getApiKey(AiProviderType.CLAUDE))
    }

    @Test
    fun testClearAllKeys() {
        storage.saveApiKey(AiProviderType.OPENAI, "key-1")
        storage.saveApiKey(AiProviderType.GEMINI, "key-2")

        assertTrue(storage.hasApiKey(AiProviderType.OPENAI))
        assertTrue(storage.hasApiKey(AiProviderType.GEMINI))

        storage.clearAllKeys()

        assertFalse(storage.hasApiKey(AiProviderType.OPENAI))
        assertFalse(storage.hasApiKey(AiProviderType.GEMINI))
    }

    @Test
    fun testEndpointAndModelPersistence() {
        // Defaults
        assertEquals(AiProviderType.GEMINI.defaultBaseUrl, storage.getEndpoint(AiProviderType.GEMINI))
        assertEquals(AiProviderType.GEMINI.defaultModel, storage.getModel(AiProviderType.GEMINI))

        // Custom endpoint and model
        val customEndpoint = "https://custom.gemini-proxy.com/v1/"
        val customModel = "gemini-2.5-pro"
        storage.saveEndpoint(AiProviderType.GEMINI, customEndpoint)
        storage.saveModel(AiProviderType.GEMINI, customModel)

        assertEquals(customEndpoint, storage.getEndpoint(AiProviderType.GEMINI))
        assertEquals(customModel, storage.getModel(AiProviderType.GEMINI))

        // Clear all keys also resets endpoint/model
        storage.clearAllKeys()
        assertEquals(AiProviderType.GEMINI.defaultBaseUrl, storage.getEndpoint(AiProviderType.GEMINI))
        assertEquals(AiProviderType.GEMINI.defaultModel, storage.getModel(AiProviderType.GEMINI))
    }

    @Test
    fun testDeepLKeyPersistence() {
        assertFalse(storage.hasDeepLKey())
        assertNull(storage.getDeepLKey())

        val deepLKey = "12345678-abcd-1234-abcd-1234567890ab:fx"
        storage.saveDeepLKey(deepLKey)

        assertTrue(storage.hasDeepLKey())
        assertEquals(deepLKey, storage.getDeepLKey())

        storage.clearDeepLKey()
        assertFalse(storage.hasDeepLKey())
        assertNull(storage.getDeepLKey())
    }

    @Test
    fun testTranslationEnginePersistence() {
        assertEquals("GOOGLE_TRANSLATE", storage.getTranslationEngine())

        storage.saveTranslationEngine("DEEPL")
        assertEquals("DEEPL", storage.getTranslationEngine())

        storage.saveTranslationEngine("AI_GROUNDED")
        assertEquals("AI_GROUNDED", storage.getTranslationEngine())
    }
}
