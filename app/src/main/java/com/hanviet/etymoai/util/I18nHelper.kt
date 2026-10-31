package com.hanviet.etymoai.util

import android.content.Context
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage(val code: String, val title: String, val flag: String, val locale: Locale) {
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳", Locale("vi", "VN")),
    KOREAN("ko", "한국어", "🇰🇷", Locale.KOREA),
    ENGLISH("en", "English", "🇺🇸", Locale.US);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: VIETNAMESE
        }
    }
}

/**
 * I18nHelper: Hỗ trợ định dạng số, ngày giờ và ngôn ngữ hiển thị địa phương hóa (i18n)
 */
object I18nHelper {

    fun getCurrentLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences("appearance_settings", Context.MODE_PRIVATE)
        val code = prefs.getString("key_app_language", "vi") ?: "vi"
        return AppLanguage.fromCode(code)
    }

    fun formatNumber(number: Long, context: Context): String {
        val lang = getCurrentLanguage(context)
        val nf = NumberFormat.getInstance(lang.locale)
        return nf.format(number)
    }

    fun formatDate(timestamp: Long, context: Context): String {
        val lang = getCurrentLanguage(context)
        val pattern = when (lang) {
            AppLanguage.VIETNAMESE -> "dd/MM/yyyy"
            AppLanguage.KOREAN -> "yyyy년 MM월 dd일"
            AppLanguage.ENGLISH -> "MMM dd, yyyy"
        }
        val sdf = SimpleDateFormat(pattern, lang.locale)
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDays(days: Int, context: Context): String {
        val lang = getCurrentLanguage(context)
        return when (lang) {
            AppLanguage.VIETNAMESE -> "$days ngày trước"
            AppLanguage.KOREAN -> "${days}일 전"
            AppLanguage.ENGLISH -> if (days <= 1) "1 day ago" else "$days days ago"
        }
    }
}
