package com.hanviet.etymoai.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import java.util.Locale

/**
 * TtsManager: Quản lý phát âm giọng nói Text-To-Speech tiếng Hàn chuẩn xác trên toàn bộ ứng dụng.
 * Hỗ trợ phát âm từ vựng, câu ví dụ, điều chỉnh tốc độ nói và tự động giải phóng tài nguyên.
 */
class TtsManager private constructor(private val appContext: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    @Volatile private var isInitialized = false
    @Volatile private var isKoreanSupported = false
    private var pendingWord: String? = null
    private var pendingSpeed: Float = 0.95f

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            // Khởi tạo TextToSpeech với Application Context để tránh rò rỉ bộ nhớ
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi tạo đối tượng TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts
            if (ttsEngine != null) {
                // Thiết lập AudioAttributes tối ưu cho phát âm từ vựng
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    ttsEngine.setAudioAttributes(audioAttributes)
                } catch (e: Exception) {
                    Log.w(TAG, "Không thể thiết lập AudioAttributes", e)
                }

                // Thử cài đặt ngôn ngữ tiếng Hàn với các Locale tương đương
                val localesToTry = listOf(
                    Locale.KOREAN,
                    Locale.KOREA,
                    Locale("ko", "KR"),
                    Locale("ko")
                )

                var langAvailable = false
                for (locale in localesToTry) {
                    val langResult = ttsEngine.setLanguage(locale)
                    if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i(TAG, "Đã kích hoạt giọng đọc tiếng Hàn với locale: ${locale.toLanguageTag()}")
                        langAvailable = true
                        isKoreanSupported = true
                        break
                    }
                }

                if (!langAvailable) {
                    Log.w(TAG, "Thiết bị chưa cài sẵn gói giọng đọc tiếng Hàn (LANG_MISSING_DATA/NOT_SUPPORTED).")
                    isKoreanSupported = false
                }

                ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "Bắt đầu phát âm: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "Hoàn tất phát âm: $utteranceId")
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "Lỗi trong quá trình phát âm utteranceId: $utteranceId")
                    }
                })

                isInitialized = true

                // Nếu có yêu cầu phát âm đang chờ trong lúc khởi tạo
                pendingWord?.let { word ->
                    speak(word, pendingSpeed)
                    pendingWord = null
                }
            }
        } else {
            Log.e(TAG, "Khởi tạo TextToSpeech thất bại với mã lỗi hệ thống: $status")
            isInitialized = false
        }
    }

    /**
     * Phát âm từ hoặc câu tiếng Hàn.
     * @param text Văn bản tiếng Hàn cần phát âm
     * @param speechRate Tốc độ phát âm (0.5f - 1.5f, mặc định 0.95f để người học nghe rõ)
     */
    fun speak(text: String, speechRate: Float = 0.95f) {
        val clean = text.trim()
        if (clean.isBlank()) return

        if (!isInitialized || tts == null) {
            pendingWord = clean
            pendingSpeed = speechRate
            Toast.makeText(appContext, "Đang khởi tạo bộ đọc phát âm tiếng Hàn...", Toast.LENGTH_SHORT).show()
            initializeTts()
            return
        }

        if (!isKoreanSupported) {
            Toast.makeText(
                appContext,
                "Thiết bị chưa có dữ liệu giọng nói tiếng Hàn (Google TTS). Vui lòng tải gói tiếng Hàn trong Cài đặt máy.",
                Toast.LENGTH_LONG
            ).show()
            openTtsSettings()
            return
        }

        try {
            val ttsEngine = tts ?: return
            ttsEngine.stop() // Dừng câu trước nếu đang phát
            ttsEngine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            ttsEngine.setPitch(1.0f)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "etymo_tts_${System.currentTimeMillis()}")
            }
            ttsEngine.speak(clean, TextToSpeech.QUEUE_FLUSH, params, "etymo_tts_id")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi phát âm: $clean", e)
            Toast.makeText(appContext, "Lỗi phát âm: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mở cài đặt TTS của hệ thống Android để người dùng tải gói tiếng Hàn
     */
    fun openTtsSettings() {
        try {
            val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Không thể mở màn hình cài đặt TTS", e2)
            }
        }
    }

    /**
     * Dừng phát âm và giải phóng tài nguyên TTS khi thoát ứng dụng
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            isKoreanSupported = false
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi đóng TextToSpeech", e)
        }
    }

    companion object {
        private const val TAG = "TtsManager"

        @Volatile
        private var instance: TtsManager? = null

        fun getInstance(context: Context): TtsManager {
            return instance ?: synchronized(this) {
                instance ?: TtsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
