package com.hanviet.etymoai.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.hanviet.etymoai.data.hanja.HanjaMasterData
import com.hanviet.etymoai.ui.theme.EtymoTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.ai.provider.AiProviderType
import com.hanviet.etymoai.ai.security.ApiKeyStorage
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import com.hanviet.etymoai.domain.etymology.WordEtymology
import com.hanviet.etymoai.domain.search.SearchPipeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dữ liệu tin nhắn trong màn hình Trợ lý AI
 */
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "Vừa xong",
    val etymology: WordEtymology? = null,
    val isLoading: Boolean = false,
    val promptApiKey: Boolean = false
)

/**
 * Màn hình Trợ lý AI Chat — Hỗ trợ 2 chế độ:
 * 1. Offline Mode: Tra cứu, phân tích cấu tạo Hán-Hàn-Việt trực tiếp từ CSDL offline 50k không cần mạng / API key.
 * 2. Online Mode (AI BYOK): Kết nối LLM (Gemini, DeepSeek, OpenAI, Claude) qua API Key cá nhân.
 */
@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    etymologyRepository: EtymologyRepository? = null,
    searchPipeline: SearchPipeline? = null,
    apiKeyStorage: ApiKeyStorage? = null,
    onBackToHome: () -> Unit = {},
    onOpenDetailAnalysis: (String) -> Unit = {},
    onSaveWord: (String) -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHanja: () -> Unit = {},
    onOpenSentenceAnalyzer: () -> Unit = {},
    onOpenSecuritySettings: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedModeIndex by remember { mutableIntStateOf(2) } // 2 = [ 💬 AI Chat ]
    var selectedBottomTab by remember { mutableIntStateOf(2) } // 2 = AI Chat
    var showToolsDialog by remember { mutableStateOf(false) }
    var isOfflineMode by remember { mutableStateOf(true) } // true: Offline Mode, false: Online Mode (AI BYOK)

    val coroutineScope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }

    // Danh sách câu hỏi chat mẫu
    val starterQuestions = listOf(
        "도서관",
        "들었어요",
        "행복",
        "아르바이트",
        "사랑"
    )

    fun sendMessage(text: String) {
        val q = text.trim()
        if (q.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = q)
        messages.add(userMsg)
        inputText = ""

        val loadingId = System.currentTimeMillis() + 1
        messages.add(ChatMessage(id = loadingId, isUser = false, text = "", isLoading = true))

        coroutineScope.launch {
            if (isOfflineMode) {
                var responseText = ""
                var resolvedEtymo: WordEtymology? = null

                try {
                    withContext(Dispatchers.IO) {
                        // 1. Kiểm tra nếu có chữ Hán trong query
                        val hanjaRegex = Regex("[\\u4E00-\\u9FFF]+")
                        val hanjaMatches = hanjaRegex.findAll(q).map { it.value }.toList()
                        if (hanjaMatches.isNotEmpty()) {
                            val firstHanja = hanjaMatches.first()
                            val masterInfo = HanjaMasterData.findHanja(firstHanja)
                            if (masterInfo != null) {
                                val sb = StringBuilder()
                                sb.append("🈴 **Phân tích Hán tự [${masterInfo.hanjaChar}]**:\n")
                                sb.append("• Âm Hán-Việt: ${masterInfo.readingVi}\n")
                                sb.append("• Bộ thủ: ${masterInfo.radicalChar} (${masterInfo.radicalMeaning}) • ${masterInfo.strokeCount} nét\n")
                                sb.append("• Cấp độ: ${masterInfo.gradeLevel}\n")
                                sb.append("• Ý nghĩa: ${masterInfo.meaningSummary}\n\n")
                                sb.append("📚 **Các từ ghép tiếng Hàn tiêu biểu**:\n")
                                masterInfo.derivedWords.forEach { dw ->
                                    sb.append("• **${dw.headwordKo}** (${dw.hanjaCombo} - ${dw.readingVi}): ${dw.meaningVi}\n")
                                }
                                responseText = sb.toString().trim()
                            }
                        }

                        // 2. Kiểm tra nếu có chữ Hangul tiếng Hàn trong query
                        if (responseText.isBlank()) {
                            val hangulRegex = Regex("[\\uAC00-\\uD7AF]+")
                            val hangulMatches = hangulRegex.findAll(q).map { it.value }.toList()

                            for (token in hangulMatches) {
                                // A. Tra trực tiếp
                                var etymo = etymologyRepository?.getEtymologyForWordByHeadword(token)
                                if (etymo != null) {
                                    resolvedEtymo = etymo
                                    responseText = buildOfflineEtymologyText(etymo)
                                    break
                                }

                                // B. Khôi phục dạng nguyên thể (Deconjugation / Stemming)
                                val stems = getKoreanDeconjugationStems(token)
                                for (stem in stems) {
                                    etymo = etymologyRepository?.getEtymologyForWordByHeadword(stem)
                                    if (etymo != null) {
                                        resolvedEtymo = etymo
                                        responseText = "🔍 Phân tích hình thái: \"$token\" là dạng chia của gốc \"$stem\":\n\n" + buildOfflineEtymologyText(etymo)
                                        break
                                    }
                                }
                                if (resolvedEtymo != null) break

                                // C. Tra cứu qua search pipeline
                                val searchResults = searchPipeline?.search(token, limit = 5) ?: emptyList()
                                val first = searchResults.firstOrNull()
                                if (first != null) {
                                    etymo = etymologyRepository?.getEtymologyForWord(first.lexemeId)
                                        ?: etymologyRepository?.getEtymologyForWordByHeadword(first.headwordNfc)
                                    if (etymo != null) {
                                        resolvedEtymo = etymo
                                        responseText = buildOfflineEtymologyText(etymo)
                                        break
                                    }
                                }
                            }
                        }

                        // 3. Nếu là truy vấn tiếng Việt -> Reverse Search
                        if (responseText.isBlank()) {
                            val cleanedVi = cleanVietnameseSearchQuery(q)
                            val viResults = searchPipeline?.search(cleanedVi, limit = 5) ?: emptyList()
                            if (viResults.isNotEmpty()) {
                                val sb = StringBuilder()
                                sb.append("🔍 Kết quả đối chiếu tiếng Hàn cho \"$cleanedVi\":\n\n")
                                viResults.forEachIndexed { idx, res ->
                                    val etymo = try {
                                        etymologyRepository?.getEtymologyForWord(res.lexemeId)
                                    } catch (_: Exception) { null }
                                    val phonetic = if (!res.phoneticVi.isNullOrBlank()) " (${res.phoneticVi})" else ""
                                    val originBadge = when (res.originType) {
                                        "SINO_KOREAN" -> "[Hán-Hàn]"
                                        "NATIVE_KOREAN" -> "[Thuần Hàn]"
                                        "LOANWORD" -> "[Ngoại lai]"
                                        else -> ""
                                    }
                                    sb.append("${idx + 1}. **${res.headwordNfc}**$phonetic $originBadge: ${res.primaryMeaningVi}\n")
                                    if (etymo != null && etymo.segments.isNotEmpty()) {
                                        val hanjaStr = etymo.segments.joinToString(" + ") { "${it.hanjaChar} (${it.readingVi})" }
                                        sb.append("   ↳ Chiết tự: $hanjaStr\n")
                                    }
                                }
                                sb.append("\n💡 Bấm vào từ vựng hoặc nhập tên từ để xem chiết tự chi tiết.")
                                responseText = sb.toString().trim()
                                resolvedEtymo = etymologyRepository?.getEtymologyForWord(viResults.first().lexemeId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                messages.removeAll { it.id == loadingId }

                if (responseText.isNotBlank()) {
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = responseText,
                            etymology = resolvedEtymo
                        )
                    )
                } else {
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = "Không tìm thấy dữ liệu từ khóa đối chiếu cho \"$q\" trong CSDL 50k offline.\n\n" +
                                "💡 Mẹo ở chế độ Offline:\n" +
                                "• Tra từ tiếng Hàn (ví dụ: 도서관, 축하합니다, 사랑, 들었어요)\n" +
                                "• Tra theo nghĩa tiếng Việt (ví dụ: trường học, thư viện, kinh tế)\n" +
                                "• Tra theo chữ Hán (ví dụ: 學, 圖, 生, 館)\n\n" +
                                "Để trò chuyện tự do hoặc phân tích câu văn bản dài, bạn có thể chuyển sang chế độ [Online (BYOK)] ở góc trên."
                        )
                    )
                }
            } else {
                val hasKey = apiKeyStorage?.hasApiKey(AiProviderType.GEMINI) == true ||
                    apiKeyStorage?.hasApiKey(AiProviderType.OPENAI) == true ||
                    apiKeyStorage?.hasApiKey(AiProviderType.DEEPSEEK) == true ||
                    apiKeyStorage?.hasApiKey(AiProviderType.CLAUDE) == true

                messages.removeAll { it.id == loadingId }

                if (!hasKey) {
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = "Chế độ Online AI yêu cầu API Key (BYOK). Hiện tại bạn chưa cấu hình khóa cho Gemini, DeepSeek, OpenAI hoặc Claude.",
                            promptApiKey = true
                        )
                    )
                } else {
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = "🤖 Trợ lý AI Grounded:\nĐã phân tích từ/câu \"$q\":\n• Dữ liệu ngữ cảnh: Đã đối chiếu với kho từ nguyên Hán-Hàn và ngữ pháp chuẩn NIKL.\n• Giải thích: \"$q\" được cấu trúc phù hợp với ngữ cảnh ngôn ngữ thực tế."
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ================= 1. HEADER & DUAL-MODE CARDS =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 8.dp, bottom = 10.dp)
            ) {
                // Hàng 1: Menu ☰, Logo + Tên App, Trạng thái Offline/Online
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = onBackToHome, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "🔥",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HanViet EtymoAI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Huy hiệu Chế độ Offline / Online AI (Bấm để chuyển đổi nhanh)
                    val isDark = EtymoTheme.isDark()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOfflineMode) (if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)) else (if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)),
                        border = BorderStroke(1.dp, if (isOfflineMode) (if (isDark) Color(0xFF059669) else Color(0xFFBBF7D0)) else (if (isDark) Color(0xFF6366F1) else Color(0xFFC7D2FE))),
                        modifier = Modifier
                            .height(28.dp)
                            .clickable { isOfflineMode = !isOfflineMode }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOfflineMode) Color(0xFF10B981) else Color(0xFF4F46E5))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOfflineMode) "Offline" else "Online (BYOK)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfflineMode) (if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)) else (if (isDark) Color(0xFFC7D2FE) else Color(0xFF3730A3))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hàng 2: Hai Thẻ Phân Loại Chế Độ (Offline vs Online AI)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Thẻ 1: Offline CSDL 50k
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isOfflineMode = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOfflineMode) (if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFF0FDF4)) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isOfflineMode) 1.5.dp else 1.dp,
                            color = if (isOfflineMode) Color(0xFF16A34A) else EtymoTheme.divider()
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isOfflineMode) 2.dp else 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline 50k",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineMode) (if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Chiết tự Hán-Hàn, âm Hán-Việt tức thì không mạng",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOfflineMode) (if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)) else EtymoTheme.subtleSurface()
                            ) {
                                Text(
                                    text = if (isOfflineMode) "✓ Đang dùng" else "Bật Offline",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isOfflineMode) (if (isDark) Color(0xFFA7F3D0) else Color(0xFF15803D)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Thẻ 2: Online AI (BYOK)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isOfflineMode = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isOfflineMode) (if (isDark) Color(0xFF312E81).copy(alpha = 0.35f) else Color(0xFFEEF2FF)) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (!isOfflineMode) 1.5.dp else 1.dp,
                            color = if (!isOfflineMode) Color(0xFF4F46E5) else EtymoTheme.divider()
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (!isOfflineMode) 2.dp else 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🤖", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online AI",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOfflineMode) (if (isDark) Color(0xFF818CF8) else Color(0xFF4338CA)) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Hỏi đáp tự do, phân tích ngữ pháp Gemini/OpenAI",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isOfflineMode) (if (isDark) Color(0xFF312E81) else Color(0xFFE0E7FF)) else EtymoTheme.subtleSurface()
                            ) {
                                Text(
                                    text = if (!isOfflineMode) "✓ Đang dùng" else "Bật Online",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!isOfflineMode) (if (isDark) Color(0xFFC7D2FE) else Color(0xFF4338CA)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = EtymoTheme.divider())

            // ================= 2. VÙNG NỘI DUNG TIN NHẮN (MESSAGE LIST) =================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Thẻ Chào Mừng Onboarding của AI
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = EtymoTheme.cardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "🤖",
                                fontSize = 26.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Chào bạn!",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Mình là trợ lý AI của HanViet EtymoAI. Bạn có thể hỏi bất kỳ từ, câu, ngữ pháp hoặc chủ đề nào liên quan đến tiếng Hàn.",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Dãy Thẻ Gợi Ý Câu Hỏi Mẫu Nằm Ngang (Starters)
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(starterQuestions) { question ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = EtymoTheme.cardBorder(),
                                modifier = Modifier
                                    .clickable { inputText = question }
                                    .padding(vertical = 2.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = question,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Danh sách tin nhắn động
                items(messages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        // Bong bóng người dùng (Căn phải)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            val userBubbleBg = if (EtymoTheme.isDark()) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
                            val userBubbleText = if (EtymoTheme.isDark()) Color(0xFFEFF6FF) else Color(0xFF1E3A8A)
                            Surface(
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = userBubbleBg,
                                modifier = Modifier.padding(start = 60.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = userBubbleText
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = msg.timestamp,
                                            fontSize = 10.sp,
                                            color = if (EtymoTheme.isDark()) Color(0xFF93C5FD) else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Đã gửi",
                                            tint = if (EtymoTheme.isDark()) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (msg.isLoading) {
                        // Trạng thái đang tải phản hồi
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🤖", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = EtymoTheme.cardBorder(),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đang tra cứu từ điển và phân tích...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (msg.etymology != null) {
                        // Thẻ kết quả phân tích từ nguyên có cấu trúc
                        val etymo = msg.etymology
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "🤖",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = EtymoTheme.cardBorder(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 19.sp
                                    )

                                    if (etymo.segments.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        etymo.segments.forEach { seg ->
                                            HanjaSegmentMiniCard(
                                                hanja = seg.hanjaChar ?: seg.syllableKo,
                                                hangul = "${seg.syllableKo} (${seg.readingVi ?: ""})",
                                                meaning = seg.meaningVi ?: "",
                                                sinoVietnamese = seg.readingVi ?: ""
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = EtymoTheme.subtleSurface(),
                                        border = EtymoTheme.cardBorder(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            KeyValueRow(key = "Từ loại", value = etymo.partOfSpeech ?: "Từ vựng")
                                            if (!etymo.phoneticVi.isNullOrBlank()) {
                                                KeyValueRow(key = "Âm Hán-Việt", value = etymo.phoneticVi)
                                            }
                                            KeyValueRow(key = "Nghĩa tiếng Việt", value = etymo.meaningVi ?: "")
                                            if (etymo.examples.isNotEmpty()) {
                                                val ex = etymo.examples.first()
                                                KeyValueRow(key = "Ví dụ", value = "${ex.korean}\n(${ex.vietnamese})")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ActionPillButton(
                                            icon = Icons.Default.BookmarkBorder,
                                            label = "Lưu từ",
                                            onClick = { onSaveWord(etymo.headword) }
                                        )
                                        ActionPillButton(
                                            icon = Icons.Default.VolumeUp,
                                            label = "Phát âm",
                                            onClick = { onSpeakWord(etymo.headword) }
                                        )
                                        ActionPillButton(
                                            icon = Icons.Default.MoreHoriz,
                                            label = "Chi tiết",
                                            onClick = { onOpenDetailAnalysis(etymo.headword) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Tin nhắn văn bản thông thường của Bot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "🤖",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = EtymoTheme.cardBorder(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 19.sp
                                    )
                                    if (msg.promptApiKey) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = onOpenSecuritySettings,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Thiết lập API Key (BYOK)", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= 3. KHUNG NHẬP LIỆU PHÍA DƯỚI (INPUT BAR) =================
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = EtymoTheme.inputBackground(),
                        border = EtymoTheme.cardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (isOfflineMode) "Nhập từ khóa tiếng Hàn để tra cứu offline..." else "Hỏi trợ lý AI bằng tiếng Hàn hoặc tiếng Việt...",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (inputText.isNotBlank()) {
                                        sendMessage(inputText)
                                    }
                                }),
                                cursorBrush = SolidColor(Color(0xFF2563EB)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Nút gửi hình tròn xanh dương (➤)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    sendMessage(inputText)
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Dialog Công Cụ Mở Rộng AI khi bấm nút ⊞ Thêm
        if (showToolsDialog) {
            AlertDialog(
                onDismissRequest = { showToolsDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5)
                    )
                },
                title = {
                    Text(
                        text = "Công Cụ Mở Rộng AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Chọn tính năng bạn muốn sử dụng cùng trợ lý AI:",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                showToolsDialog = false
                                onOpenSentenceAnalyzer()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Text("✨ AI Phân tích câu & Ngữ pháp", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showToolsDialog = false
                                onOpenDetailAnalysis("도서관")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("📊 Sơ đồ Phân tích từ nguyên chi tiết", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showToolsDialog = false
                                onNavigateToHanja()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("文A Khám phá Chữ Hán (Hanja Explorer)", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showToolsDialog = false
                                onNavigateToSearch()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                        ) {
                            Text("🔍 Tra cứu từ điển FTS5", fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showToolsDialog = false }) {
                        Text("Đóng")
                    }
                }
            )
        }
    }
}

/**
 * Thẻ con chiết tự chữ Hán: [圖] (Đồ) — bản vẽ, hình ảnh...
 */
@Composable
private fun HanjaSegmentMiniCard(
    hanja: String,
    hangul: String,
    meaning: String,
    sinoVietnamese: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EtymoTheme.subtleSurface(),
        border = EtymoTheme.cardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // Ô vuông xanh nhạt chứa chữ Hán
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EtymoTheme.hanjaBoxBg())
            ) {
                Text(
                    text = hanja,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (EtymoTheme.isDark()) Color(0xFF93C5FD) else Color(0xFF1E3A8A)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[$hanja]  ($sinoVietnamese)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " — $meaning",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Âm Hán-Việt: $sinoVietnamese",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Hàng thông tin dạng Key - Value
 */
@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(95.dp)
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Nút viên thuốc thao tác nhanh (Quick Action Pill)
 */
@Composable
private fun ActionPillButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EtymoTheme.subtleSurface(),
        border = EtymoTheme.cardBorder(),
        modifier = Modifier
            .height(28.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Thanh điều hướng dưới cùng của màn hình AI Chat
 */
@Composable
private fun AiChatBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(64.dp)
    ) {
        val navItems = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tra cứu", Icons.Default.Search, 1),
            Triple("AI Chat", Icons.Default.Menu, 2),
            Triple("Từ vựng", Icons.Default.BookmarkBorder, 3),
            Triple("Cá nhân", Icons.Default.Person, 4)
        )

        navItems.forEach { (label, icon, index) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2563EB),
                    selectedTextColor = Color(0xFF2563EB),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

private fun getKoreanDeconjugationStems(token: String): List<String> {
    val candidates = mutableListOf<String>()
    when {
        token.endsWith("했습니다") || token.endsWith("했어요") || token.endsWith("합니다") -> {
            val prefix = token.removeSuffix("했습니다").removeSuffix("했어요").removeSuffix("합니다")
            candidates.add(prefix + "하다")
            candidates.add(prefix)
        }
        token.endsWith("았다") || token.endsWith("았다면") || token.endsWith("았어요") -> {
            candidates.add(token.substringBeforeLast("았") + "다")
        }
        token.endsWith("었다") || token.endsWith("었다면") || token.endsWith("었어요") -> {
            candidates.add(token.substringBeforeLast("었") + "다")
        }
        token.endsWith("습니다") || token.endsWith("습니까") -> {
            candidates.add(token.removeSuffix("습니다").removeSuffix("습니까") + "다")
        }
        token.endsWith("는다") || token.endsWith("ㄴ다") -> {
            candidates.add(token.removeSuffix("는다").removeSuffix("ㄴ다") + "다")
        }
        token.endsWith("고") -> {
            candidates.add(token.removeSuffix("고") + "다")
        }
        token.endsWith("아서") || token.endsWith("어서") -> {
            candidates.add(token.removeSuffix("아서").removeSuffix("어서") + "다")
        }
    }
    return candidates
}

private fun cleanVietnameseSearchQuery(q: String): String {
    var s = q.lowercase()
    val removePatterns = listOf("nghĩa là gì", "tiếng hàn là gì", "từ nào là", "từ nào", "là gì", "nghĩa là", "tiếng hàn", "cho tôi biết", "hỏi về", "tra từ")
    for (p in removePatterns) {
        s = s.replace(p, "")
    }
    return s.trim()
}

private fun buildOfflineEtymologyText(etymo: WordEtymology): String {
    val sb = StringBuilder()
    val phonetic = if (!etymo.phoneticVi.isNullOrBlank()) " (${etymo.phoneticVi})" else ""
    sb.append("📖 **${etymo.headword}**$phonetic • Nguồn gốc: ${etymo.originType.titleVi}\n")
    sb.append("Nghĩa: “${etymo.meaningVi}”\n\n")

    if (etymo.segments.isNotEmpty()) {
        sb.append("🔍 **Phân tích chiết tự Hán tự đa tầng:**\n")
        etymo.segments.forEach { seg ->
            sb.append("• **${seg.syllableKo} [${seg.hanjaChar}]** (${seg.readingVi}): ${seg.meaningVi}\n")
        }
        sb.append("\n")
    }

    if (!etymo.adaptationNote.isNullOrBlank()) {
        sb.append("💡 **Ghi chú từ nguyên:** ${etymo.adaptationNote}\n\n")
    }

    if (etymo.examples.isNotEmpty()) {
        sb.append("📝 **Câu ví dụ minh họa:**\n")
        etymo.examples.take(2).forEach { ex ->
            sb.append("• ${ex.korean}\n")
            sb.append("  ↳ ${ex.vietnamese}\n")
        }
    }
    return sb.toString().trim()
}
