package com.hanviet.etymoai.util

import android.content.Context
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
class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingWord: String? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khởi tạo TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Ngôn ngữ tiếng Hàn (ko-KR) chưa được cài đặt trong máy, thử fallback Locale.KOREA")
                val fallbackResult = tts?.setLanguage(Locale.KOREA)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Thiết bị không hỗ trợ phát âm tiếng Hàn TTS.")
                }
            }
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {}
                override fun onError(utteranceId: String?) {}
            })

            pendingWord?.let { word ->
                speak(word)
                pendingWord = null
            }
        } else {
            Log.e(TAG, "Khởi tạo TextToSpeech thất bại với mã lỗi: $status")
        }
    }

    /**
     * Phát âm từ hoặc câu tiếng Hàn.
     * @param text Văn bản tiếng Hàn cần phát âm
     * @param speechRate Tốc độ phát âm (0.75f - 1.25f, mặc định 0.95f để người học nghe rõ)
     */
    fun speak(text: String, speechRate: Float = 0.95f) {
        val clean = text.trim()
        if (clean.isBlank()) return

        if (!isInitialized || tts == null) {
            pendingWord = clean
            Toast.makeText(context, "Đang khởi tạo bộ đọc phát âm tiếng Hàn...", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(1.0f)
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "hanviet_tts_${System.currentTimeMillis()}")
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, params, "hanviet_tts_id")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi phát âm: $clean", e)
            Toast.makeText(context, "Lỗi phát âm: ${e.message}", Toast.LENGTH_SHORT).show()
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
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi đóng TextToSpeech", e)
        }
    }

    companion object {
        private const val TAG = "TtsManager"
    }
}
