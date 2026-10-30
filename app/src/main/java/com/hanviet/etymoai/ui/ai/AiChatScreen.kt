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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

/**
 * Màn hình Trợ lý AI Chat — Tra cứu từ Hán-Hàn (도서관)
 * Tái hiện chính xác bản vẽ Wireframe Màn hình 1 theo chuẩn Idea 2.2.
 */
@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    onBackToHome: () -> Unit = {},
    onOpenDetailAnalysis: (String) -> Unit = {},
    onSaveWord: (String) -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedModeIndex by remember { mutableIntStateOf(2) } // 2 = [ 💬 AI Chat ]
    var selectedBottomTab by remember { mutableIntStateOf(2) } // 2 = AI Chat

    // Danh sách câu hỏi chat mẫu
    val starterQuestions = listOf(
        "⭐ Giải thích ý nghĩa của từ?",
        "💬 Phân biệt từ đồng nghĩa?",
        "⇄ Phân tích cấu trúc câu?"
    )

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

                    // Huy hiệu [● Offline]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
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
                                .clickable { selectedModeIndex = index }
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

                // Bóng Chat Người Dùng Hỏi: "도서관" (Căn phải)
                item {
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
                                    text = "도서관",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "09:41",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Đã xem",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Thẻ Phản Hồi Có Cấu Trúc Của AI (Structured Bot Response Card)
                item {
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
                                // Đoạn giới thiệu
                                Text(
                                    text = "도서관 (圖書館) là một từ Hán-Hàn, có nghĩa là “thư viện”. Từ này được cấu tạo từ 3 chữ Hán:",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Danh sách 3 Thẻ Chiết Tự Chữ Hán: 圖, 書, 館
                                HanjaSegmentMiniCard(
                                    hanja = "圖",
                                    hangul = "도 (Đồ)",
                                    meaning = "bản vẽ, hình ảnh, sách",
                                    sinoVietnamese = "Đồ"
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HanjaSegmentMiniCard(
                                    hanja = "書",
                                    hangul = "서 (Thư)",
                                    meaning = "sách, văn bản",
                                    sinoVietnamese = "Thư"
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HanjaSegmentMiniCard(
                                    hanja = "館",
                                    hangul = "관 (Quán)",
                                    meaning = "tòa nhà, cơ quan",
                                    sinoVietnamese = "Quán"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bảng Thông Tin Từ Điển (2 cột Key-Value)
                                Text(
                                    text = "Thông tin từ điển:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        KeyValueRow(key = "Từ loại", value = "Danh từ")
                                        KeyValueRow(key = "Cấp độ", value = "Trung cấp")
                                        KeyValueRow(key = "Phiên âm", value = "do-seo-gwan")
                                        KeyValueRow(key = "Nghĩa tiếng Việt", value = "Thư viện")
                                        KeyValueRow(key = "Ví dụ", value = "도서관에 가요. (Tôi đi thư viện.)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Khối Từ Nguyên & Liên Quan
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "💡 Từ nguyên và liên quan:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Là từ Hán-Hàn, thuộc nhóm từ gốc Hán.\n• Có nhiều từ cùng gốc: 도서 (sách), 도서관 (thư viện), 도서대 (giá sách), 독서 (đọc)...",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 17.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Hàng Nút Thao Tác Nhanh (Pills)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ActionPillButton(
                                        icon = Icons.Default.BookmarkBorder,
                                        label = "Lưu từ vựng",
                                        onClick = { onSaveWord("도서관") }
                                    )
                                    ActionPillButton(
                                        icon = Icons.Default.VolumeUp,
                                        label = "Phát âm",
                                        onClick = { onSpeakWord("도서관") }
                                    )
                                    ActionPillButton(
                                        icon = Icons.Default.MoreHoriz,
                                        label = "Xem thêm",
                                        onClick = { onOpenDetailAnalysis("아르바이트") }
                                    )
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
                                    text = "Hỏi thêm về từ này...",
                                    fontSize = 13.sp,
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
                                        onOpenDetailAnalysis(inputText.trim())
                                        inputText = ""
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
                                    onOpenDetailAnalysis(inputText.trim())
                                    inputText = ""
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
