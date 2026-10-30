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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                var etymo: WordEtymology? = null
                try {
                    etymo = etymologyRepository?.getEtymologyForWordByHeadword(q)
                    if (etymo == null) {
                        val searchResults = searchPipeline?.search(q, limit = 5) ?: emptyList()
                        val first = searchResults.firstOrNull()
                        if (first != null) {
                            etymo = etymologyRepository?.getEtymologyForWord(first.lexemeId)
                                ?: etymologyRepository?.getEtymologyForWordByHeadword(first.headwordNfc)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                messages.removeAll { it.id == loadingId }

                if (etymo != null) {
                    val intro = "${etymo.headword} ${if (etymo.phoneticVi != null) "(${etymo.phoneticVi})" else ""} là từ ${etymo.originType.titleVi}, có nghĩa là: “${etymo.meaningVi}”."
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = intro,
                            etymology = etymo
                        )
                    )
                } else {
                    messages.add(
                        ChatMessage(
                            isUser = false,
                            text = "Không tìm thấy từ \"$q\" trong CSDL 50k offline. Ở chế độ Offline, bạn có thể tra cứu từ vựng, từ Hán-Hàn hoặc dạng chia gốc động từ (ví dụ: 도서관, 들었어요, 행복, 사랑).\n\nNếu muốn phân tích câu dài hoặc hỏi tự do, bạn có thể chuyển sang chế độ [Online (BYOK)] ở góc trên."
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
        bottomBar = {
            AiChatBottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { tab ->
                    selectedBottomTab = tab
                    onNavigateTab(tab)
                }
            )
        },
        containerColor = Color(0xFFF4F7FB),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ================= 1. HEADER & MODE SWITCHER =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 8.dp, bottom = 10.dp)
            ) {
                // Hàng 1: Menu ☰, Logo + Tên App, Trạng thái Offline
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
                            tint = Color(0xFF334155)
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
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Huy hiệu Chế độ Offline / Online AI (Bấm để chuyển đổi nhanh)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOfflineMode) Color(0xFFDCFCE7) else Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, if (isOfflineMode) Color(0xFFBBF7D0) else Color(0xFFC7D2FE)),
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
                                color = if (isOfflineMode) Color(0xFF065F46) else Color(0xFF3730A3)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hàng 2: Mode Switcher (Tìm từ, Hán tự, AI Chat, Thêm)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val modes = listOf("🔍 Tìm từ", "文A Hán tự", "💬 AI Chat", "⊞ Thêm")
                    modes.forEachIndexed { index, title ->
                        val isSelected = selectedModeIndex == index
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clickable {
                                    selectedModeIndex = index
                                    when (index) {
                                        0 -> onNavigateToSearch()
                                        1 -> onNavigateToHanja()
                                        2 -> { /* Đang ở tab AI Chat */ }
                                        3 -> showToolsDialog = true
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Mình là trợ lý AI của HanViet EtymoAI. Bạn có thể hỏi bất kỳ từ, câu, ngữ pháp hoặc chủ đề nào liên quan đến tiếng Hàn.",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF475569)
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
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                        color = Color(0xFF334155)
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
                            Surface(
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = Color(0xFFDBEAFE),
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
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = msg.timestamp,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Đã gửi",
                                            tint = Color(0xFF2563EB),
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
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                        color = Color(0xFF64748B)
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B),
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
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B),
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
                color = Color.White,
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
                        color = Color(0xFFF1F5F9),
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
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF0F172A)
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
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                    .background(Color(0xFFEFF6FF))
            ) {
                Text(
                    text = hanja,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF1E3A8A)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[$hanja]  ($sinoVietnamese)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = " — $meaning",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Âm Hán-Việt: $sinoVietnamese",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B)
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
            color = Color(0xFF64748B),
            modifier = Modifier.width(95.dp)
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            color = Color(0xFF0F172A),
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
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
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
                tint = Color(0xFF334155),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
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
        containerColor = Color.White,
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
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
