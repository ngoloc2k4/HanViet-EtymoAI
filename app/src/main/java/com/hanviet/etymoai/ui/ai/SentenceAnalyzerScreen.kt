package com.hanviet.etymoai.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.hanviet.etymoai.ui.theme.EtymoTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.ai.security.SecureApiKeyStorage
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.data.translation.UnifiedTranslationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalyzedSentenceSegment(
    val partKo: String,
    val posTag: String,
    val explanationVi: String,
    val hanjaOriginal: String? = null
)

/**
 * 6. Màn hình AI Phân Tích Câu & Ngữ Pháp (Contextual Sentence Analyzer Screen)
 * Hiện thực hóa Mục 13.3 (Local Grounding AI & Phân tích cấu trúc câu theo ngữ cảnh) của Idea 2.2.
 */
@Composable
fun SentenceAnalyzerScreen(
    onBackClick: () -> Unit = {},
    onWordClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storage = remember { SecureApiKeyStorage.create(context) }
    val translationService = remember { UnifiedTranslationService(storage) }
    val dbHelper = remember { CoreDatabaseHelper.getInstance(context) }

    var inputText by remember { mutableStateOf("오늘은 날씨가 정말 좋네요.") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var hasAnalyzed by remember { mutableStateOf(false) }
    var translatedSentence by remember { mutableStateOf("") }
    var activeEngineDisplayName by remember { mutableStateOf("") }
    var dynamicSegments by remember { mutableStateOf<List<AnalyzedSentenceSegment>>(emptyList()) }

    val quickExamples = remember {
        listOf(
            "이거 뭐예요?",
            "지금 뭐 해요?",
            "너무 좋다",
            "주말에 도서관에 갔어요",
            "한국어를 열심히 공부하고 있어요"
        )
    }

    Scaffold(
        topBar = {
            SentenceAnalyzerTopBar(onBackClick = onBackClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // 1. Khối trạng thái AI Grounding
                AiGroundingStatusCard()
            }

            item {
                // 2. Thẻ Tùy chọn Model / BYOK
                ModelOptionsCard()
            }

            item {
                // 3. Khung nhập văn bản / câu tiếng Hàn (Giới hạn 1000 ký tự)
                SentenceInputBox(
                    text = inputText,
                    onTextChange = {
                        if (it.length <= 1000) {
                            inputText = it
                            hasAnalyzed = false
                        }
                    }
                )
            }

            item {
                // 4. Ví dụ nhanh + Nút làm mới
                QuickExamplesRow(
                    examples = quickExamples,
                    onSelect = {
                        inputText = it
                        hasAnalyzed = false
                    }
                )
            }

            item {
                // 5. Nút ✨ Phân tích nổi bật
                Button(
                    onClick = {
                        isAnalyzing = true
                        coroutineScope.launch {
                            val transResult = translationService.translate(inputText)
                            val transText = transResult.getOrNull()?.translatedText ?: "Không thể kết nối máy dịch"
                            val engineName = transResult.getOrNull()?.engine?.displayName ?: "Google Dịch"

                            val segments = withContext(Dispatchers.IO) {
                                analyzeSentenceTokens(dbHelper, inputText)
                            }

                            translatedSentence = transText
                            activeEngineDisplayName = engineName
                            dynamicSegments = segments
                            isAnalyzing = false
                            hasAnalyzed = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0x334F46E5)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    enabled = inputText.isNotBlank() && !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang dịch câu & bóc tách cấu trúc...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(text = "✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Phân tích câu & dịch thuật",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 6. Kết quả phân tích (Hiển thị sau khi người dùng bấm Phân tích)
            if (hasAnalyzed) {
                item {
                    AnalysisResultCard(
                        inputText = inputText,
                        translatedSentence = translatedSentence,
                        engineDisplayName = activeEngineDisplayName,
                        segments = dynamicSegments,
                        onWordClick = onWordClick
                    )
                }
            }

            item {
                // 7. Thẻ Giới thiệu tính năng AI
                AiFeaturesHelpCard()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SentenceAnalyzerTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI phân tích câu & ngữ pháp",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * 1. Khối trạng thái AI Grounding
 */
@Composable
private fun AiGroundingStatusCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🤖", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "AI đang hoạt động (Local Grounding)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hỗ trợ phân tích cấu trúc ngữ pháp, bóc tách thành phần câu và liên kết trực tiếp với dữ liệu từ điển offline.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * 2. Thẻ Tùy chọn Model / BYOK
 */
@Composable
private fun ModelOptionsCard() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✦", fontSize = 13.sp, color = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gemini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Model: gemini-2.5-flash",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(EtymoTheme.divider())
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔑", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BYOK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Đã cấu hình",
                    fontSize = 11.sp,
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 3. Ô Nhập câu tiếng Hàn
 */
@Composable
private fun SentenceInputBox(
    text: String,
    onTextChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "Nhập câu tiếng Hàn hoặc dán đoạn văn bản cần phân tích...\nVí dụ: 오늘은 날씨가 정말 좋네요.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }
                    innerTextField()
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${text.length}/1000",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 4. Hàng Ví dụ nhanh
 */
@Composable
private fun QuickExamplesRow(
    examples: List<String>,
    onSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ví dụ nhanh",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Refresh examples */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Làm mới",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            examples.forEach { example ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = EtymoTheme.cardBorder(),
                    modifier = Modifier.clickable { onSelect(example) }
                ) {
                    Text(
                        text = example,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Thẻ Kết quả phân tích câu chi tiết
 */
@Composable
private fun AnalysisResultCard(
    inputText: String,
    translatedSentence: String,
    engineDisplayName: String,
    segments: List<AnalyzedSentenceSegment>,
    onWordClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kết quả phân tích ngữ pháp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (EtymoTheme.isDark()) Color(0xFF93C5FD) else Color(0xFF1E40AF)
                    )
                }

                if (engineDisplayName.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (EtymoTheme.isDark()) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFEFF6FF),
                        border = EtymoTheme.cardBorder()
                    ) {
                        Text(
                            text = engineDisplayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (EtymoTheme.isDark()) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EtymoTheme.subtleSurface(),
                border = EtymoTheme.cardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Bản dịch nghĩa:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = translatedSentence.ifBlank { "Đang dịch câu..." },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            HorizontalDivider(color = EtymoTheme.divider())

            Text(
                text = "Bóc tách thành phần câu & Từ nguyên:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            segments.forEach { seg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onWordClick(seg.partKo) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = seg.partKo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.width(70.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EtymoTheme.subtleSurface(),
                        border = EtymoTheme.cardBorder(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = seg.posTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = seg.explanationVi,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                        if (!seg.hanjaOriginal.isNullOrBlank()) {
                            Text(
                                text = "Hán tự: ${seg.hanjaOriginal}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bóc tách và tra cứu các từ tố trong câu từ CSDL từ điển Core SQLite thực tế
 */
private fun analyzeSentenceTokens(dbHelper: CoreDatabaseHelper, text: String): List<AnalyzedSentenceSegment> {
    val tokens = text.trim().split(Regex("[\\s,;:.!?，。！？~]+")).filter { it.isNotBlank() }
    val result = mutableListOf<AnalyzedSentenceSegment>()
    val db = try { dbHelper.getReadableDatabase() } catch (_: Exception) { null } ?: return emptyList()

    for (token in tokens) {
        var foundPos = "Từ vựng"
        var foundMeaning = ""
        var foundHanja: String? = null

        // 1. Tra cứu trực tiếp headword_nfc
        db.rawQuery(
            """
            SELECT l.origin_type, s.part_of_speech, s.meaning_vi,
                   (SELECT GROUP_CONCAT(seg.hanja_char || '(' || seg.reading_vi || ')', '')
                    FROM lexeme_hanja_segments seg WHERE seg.lexeme_id = l.lexeme_id) AS hanja_decomp
            FROM lexemes l
            LEFT JOIN senses s ON l.lexeme_id = s.lexeme_id
            WHERE l.headword_nfc = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(token)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val origType = cursor.getString(0)
                foundPos = if (!cursor.isNull(1)) cursor.getString(1) else "Từ vựng"
                foundMeaning = if (!cursor.isNull(2)) cursor.getString(2) else ""
                val decomp = if (!cursor.isNull(3)) cursor.getString(3) else null
                if (!decomp.isNullOrBlank()) {
                    foundHanja = decomp
                } else if (origType == "NATIVE_KOREAN") {
                    foundHanja = "Thuần Hàn"
                } else if (origType == "LOANWORD") {
                    foundHanja = "Ngoại lai"
                }
            }
        }

        // 2. Tra cứu precomputed_forms nếu không khớp trực tiếp
        if (foundMeaning.isBlank()) {
            db.rawQuery(
                """
                SELECT l.headword_nfc, s.part_of_speech, s.meaning_vi, pf.grammar_desc
                FROM precomputed_forms pf
                JOIN lexemes l ON pf.lexeme_id = l.lexeme_id
                LEFT JOIN senses s ON l.lexeme_id = s.lexeme_id
                WHERE pf.inflected_form = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(token)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val root = cursor.getString(0)
                    foundPos = if (!cursor.isNull(1)) cursor.getString(1) else "Động/Tính từ"
                    val meaning = if (!cursor.isNull(2)) cursor.getString(2) else ""
                    val desc = if (!cursor.isNull(3)) cursor.getString(3) else ""
                    foundMeaning = "$meaning (Gốc: $root - $desc)"
                }
            }
        }

        if (foundMeaning.isBlank()) {
            foundMeaning = "Thành phần câu / trợ từ"
        }

        result.add(
            AnalyzedSentenceSegment(
                partKo = token,
                posTag = foundPos,
                explanationVi = foundMeaning,
                hanjaOriginal = foundHanja
            )
        )
    }
    return result
}

/**
 * 7. Thẻ Giới thiệu tính năng AI
 */
@Composable
private fun AiFeaturesHelpCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = EtymoTheme.subtleSurface(),
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI có thể giúp gì?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            listOf(
                "Phân tích cấu trúc câu và các điểm ngữ pháp TOPIK",
                "Giải thích ý nghĩa và sắc thái văn hóa Hàn Quốc",
                "Liên kết thành tố từ vựng với kho từ điển Hán-Hàn offline",
                "Đề xuất cách diễn đạt tự nhiên hơn trong giao tiếp đời thường"
            ).forEach { feature ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
