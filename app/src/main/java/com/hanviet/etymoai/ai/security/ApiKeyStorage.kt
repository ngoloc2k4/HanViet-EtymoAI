package com.hanviet.etymoai.ai.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.hanviet.etymoai.ai.provider.AiProviderType
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Interface quản lý lưu trữ và truy xuất khóa API cá nhân (BYOK)
 */
interface ApiKeyStorage {
    fun saveApiKey(provider: AiProviderType, key: String)
    fun getApiKey(provider: AiProviderType): String?
    fun clearApiKey(provider: AiProviderType)
    fun hasApiKey(provider: AiProviderType): Boolean
    fun clearAllKeys()
    fun saveEndpoint(provider: AiProviderType, endpoint: String)
    fun getEndpoint(provider: AiProviderType): String
    fun saveModel(provider: AiProviderType, model: String)
    fun getModel(provider: AiProviderType): String
}

/**
 * Kho lưu trữ khóa API bảo mật sử dụng thuật toán mã hóa đối xứng AES-256-GCM
 * Không bao giờ lưu khóa dưới dạng plain text vào bộ nhớ ngoài hoặc logcat.
 */
class SecureApiKeyStorage(
    private val prefs: SharedPreferences,
    customKey: ByteArray? = null
) : ApiKeyStorage {

    companion object {
        private const val PREFS_NAME = "secure_byok_api_keys"
        private const val KEY_PREFIX = "byok_key_"
        private const val IV_PREFIX = "byok_iv_"
        private const val ENDPOINT_PREFIX = "byok_endpoint_"
        private const val MODEL_PREFIX = "byok_model_"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "hanviet_byok_master_key_v1"
        private const val MASTER_KEY_STORAGE = "app_master_entropy_v1"
        private const val AES_GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12 // 96 bits chuẩn cho AES-GCM

        /**
         * Tạo instance mặc định từ Android Context
         */
        fun create(context: Context): SecureApiKeyStorage {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return SecureApiKeyStorage(prefs)
        }
    }

    private val masterKey: SecretKey

    init {
        masterKey = if (customKey != null) {
            SecretKeySpec(customKey, "AES")
        } else {
            getOrCreateMasterKey()
        }
    }

    /**
     * Khởi tạo hoặc truy xuất Master Key từ phần cứng Android KeyStore.
     * Tự động fallback an toàn sang phần mềm khi chạy trong môi trường Unit Test (Robolectric/JVM).
     */
    private fun getOrCreateMasterKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGen = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: (keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey)
        } catch (_: Exception) {
            getOrCreateSoftwareFallbackKey()
        }
    }

    private fun getOrCreateSoftwareFallbackKey(): SecretKey {
        val existingBase64 = prefs.getString(MASTER_KEY_STORAGE, null)
        return if (existingBase64 != null) {
            val bytes = Base64.decode(existingBase64, Base64.NO_WRAP)
            SecretKeySpec(bytes, "AES")
        } else {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256, SecureRandom())
            val newKey = keyGen.generateKey()
            val encoded = Base64.encodeToString(newKey.encoded, Base64.NO_WRAP)
            prefs.edit().putString(MASTER_KEY_STORAGE, encoded).apply()
            newKey
        }
    }

    override fun saveApiKey(provider: AiProviderType, key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            clearApiKey(provider)
            return
        }

        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val gcmSpec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec)

            val cipherText = cipher.doFinal(trimmed.toByteArray(Charsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)

            prefs.edit()
                .putString(IV_PREFIX + provider.name, ivBase64)
                .putString(KEY_PREFIX + provider.name, cipherBase64)
                .apply()
        } catch (e: Exception) {
            throw IllegalStateException("Lỗi khi mã hóa khóa API cho ${provider.name}: ${e.message}", e)
        }
    }

    override fun getApiKey(provider: AiProviderType): String? {
        val ivBase64 = prefs.getString(IV_PREFIX + provider.name, null) ?: return null
        val cipherBase64 = prefs.getString(KEY_PREFIX + provider.name, null) ?: return null

        return try {
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipherText = Base64.decode(cipherBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec)

            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    override fun clearApiKey(provider: AiProviderType) {
        prefs.edit()
            .remove(IV_PREFIX + provider.name)
            .remove(KEY_PREFIX + provider.name)
            .apply()
    }

    override fun hasApiKey(provider: AiProviderType): Boolean {
        if (!provider.requiresApiKey) return true
        val key = getApiKey(provider)
        return !key.isNullOrBlank()
    }

    override fun clearAllKeys() {
        val editor = prefs.edit()
        AiProviderType.entries.forEach { p ->
            editor.remove(IV_PREFIX + p.name)
            editor.remove(KEY_PREFIX + p.name)
            editor.remove(ENDPOINT_PREFIX + p.name)
            editor.remove(MODEL_PREFIX + p.name)
        }
        editor.apply()
    }

    override fun saveEndpoint(provider: AiProviderType, endpoint: String) {
        val trimmed = endpoint.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove(ENDPOINT_PREFIX + provider.name).apply()
        } else {
            prefs.edit().putString(ENDPOINT_PREFIX + provider.name, trimmed).apply()
        }
    }

    override fun getEndpoint(provider: AiProviderType): String {
        val custom = prefs.getString(ENDPOINT_PREFIX + provider.name, null)
        return if (!custom.isNullOrBlank()) custom else provider.defaultBaseUrl
    }

    override fun saveModel(provider: AiProviderType, model: String) {
        val trimmed = model.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove(MODEL_PREFIX + provider.name).apply()
        } else {
            prefs.edit().putString(MODEL_PREFIX + provider.name, trimmed).apply()
        }
    }

    override fun getModel(provider: AiProviderType): String {
        val custom = prefs.getString(MODEL_PREFIX + provider.name, null)
        return if (!custom.isNullOrBlank()) custom else provider.defaultModel
    }
}
