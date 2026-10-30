package com.hanviet.etymoai.ui.ai

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Màn hình Phân tích chi tiết bằng AI — Từ ngoại lai (아르바이트)
 * Tái hiện chính xác bản vẽ Wireframe Màn hình 2 theo chuẩn Idea 2.2:
 * 1. Đã chuẩn hóa từ mượn tiếng Đức chuẩn xác (loại bỏ ảo giác về 빵, 커피, 비nu, 노래).
 * 2. Thay phần trăm độ chính xác bằng nhãn kiểm chứng học thuật [✓ Đã xác minh nguồn].
 * 3. Chuẩn hóa tiêu đề cảnh báo: "⚠️ Lưu ý chuyển nghĩa (Cách dùng tại Hàn)".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDetailAnalysisScreen(
    headword: String = "아르바이트",
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveWord: (String) -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onFindSimilar: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedBottomTab by remember { mutableIntStateOf(2) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phân tích chi tiết bằng AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSaveWord(headword) }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Lưu từ",
                            tint = Color(0xFF334155)
                        )
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Thêm",
                            tint = Color(0xFF334155)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ================= 1. BÓNG CHAT NGƯỜI DÙNG =================
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.padding(start = 50.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Giải thích giúp tôi từ “$headword” nhé",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "09:42",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Đã nhận",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ================= 2. THẺ PHÂN TÍCH CHUYÊN SÂU (DEEP-DIVE CARD) =================
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Thẻ: Robot Avatar + Headword + Nguồn gốc + Huy hiệu kiểm chứng
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "🤖", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "아르바이트 (Arbeit)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Từ ngoại lai (tiếng Đức)",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                // Huy hiệu [✓ Đã xác minh nguồn] (Chuẩn Idea 2.2 thay vì phần trăm độ tin cậy)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = "✓ Đã xác minh nguồn",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ---------------- 1. NGHĨA VÀ THÔNG TIN CƠ BẢN ----------------
                            Text(
                                text = "1. Nghĩa và thông tin cơ bản",
                                fontSize = 12.5.sp,
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
                                    DetailKeyValueRow("Từ loại", "Danh từ")
                                    DetailKeyValueRow("Phiên âm", "a-reu-ba-i-teu")
                                    DetailKeyValueRow("Nguồn gốc", "Tiếng Đức (Arbeit)")
                                    DetailKeyValueRow("Nghĩa gốc", "Công việc, lao động")
                                    DetailKeyValueRow(
                                        "Nghĩa tiếng Hàn",
                                        "Việc làm bán thời gian / làm thêm"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ---------------- 2. GIẢI THÍCH TỪ NGUYÊN ----------------
                            Text(
                                text = "2. Giải thích từ nguyên",
                                fontSize = 12.5.sp,
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
                                Text(
                                    text = "아르바이트 là từ phiên âm tiếng Đức “Arbeit” (nghĩa gốc là công việc). Khi du nhập vào tiếng Hàn qua trung gian tiếng Nhật (アルバイト - arubaito), từ này đã trải qua hiện tượng thu hẹp ngữ nghĩa, chỉ dùng để chỉ công việc làm thêm, thường là việc bán thời gian cho sinh viên hoặc người đi làm.",
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.5.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ---------------- 3. VÍ DỤ SỬ DỤNG ----------------
                            Text(
                                text = "3. Ví dụ sử dụng",
                                fontSize = 12.5.sp,
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
                                    Text(
                                        text = "• 지금 아르바이트를 하고 있어요.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "  (Hiện tại tôi đang làm thêm.)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• 주말에 아르바이트를 해요.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "  (Cuối tuần tôi làm thêm.)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ---------------- 4. KHỐI CẢNH BÁO CHUYỂN NGHĨA ----------------
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⚠️ Lưu ý chuyển nghĩa (Cách dùng tại Hàn)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Trong tiếng Hàn hiện đại, \"아르바이트\" (viết tắt thông tục là 알바 - alba) chỉ dùng với nghĩa làm thêm/bán thời gian, không dùng để chỉ công việc chính thức/nghề nghiệp ổn định (직업, 직장).",
                                        fontSize = 11.5.sp,
                                        lineHeight = 17.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // ---------------- 5. LIÊN QUAN VÀ MỞ RỘNG (ĐÃ SỬA CHUẨN XÁC) ----------------
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "🔗 Liên quan và mở rộng",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Các từ gốc ngoại lai mượn từ tiếng Đức đã xác minh học thuật:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Dãy chip các từ mượn tiếng Đức chuẩn (Allergie, Gips, Hormon)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    GermanLoanwordChip("알레르기 (Allergie)", "dị ứng", onClick = { onFindSimilar("알레르기") })
                                    GermanLoanwordChip("깁스 (Gips)", "bó bột", onClick = { onFindSimilar("깁스") })
                                    GermanLoanwordChip("호르몬 (Hormon)", "nội tiết", onClick = { onFindSimilar("호르몬") })
                                }
                            }
                        }
                    }
                }

                // ================= 3. KHỐI GỢI Ý HÀNH ĐỘNG TÁCH RỜI (💡 BẠN CÓ MUỐN) =================
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💡 Bạn có muốn:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ActionSuggestPill(
                                    icon = Icons.Default.BookmarkBorder,
                                    label = "Lưu từ vựng",
                                    onClick = { onSaveWord(headword) }
                                )
                                ActionSuggestPill(
                                    icon = Icons.Default.VolumeUp,
                                    label = "Phát âm",
                                    onClick = { onSpeakWord(headword) }
                                )
                                ActionSuggestPill(
                                    icon = Icons.Default.Search,
                                    label = "Tìm tương tự",
                                    onClick = { onFindSimilar(headword) }
                                )
                            }
                        }
                    }
                }
            }

            // ================= 4. KHUNG NHẬP LIỆU PHÍA DƯỚI (INPUT BAR) =================
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
                                        inputText = ""
                                    }
                                }),
                                cursorBrush = SolidColor(Color(0xFF2563EB)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .clickable {
                                if (inputText.isNotBlank()) {
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
 * Chip hiển thị từ mượn tiếng Đức chuẩn xác học thuật
 */
@Composable
private fun GermanLoanwordChip(
    word: String,
    meaning: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = word,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = meaning,
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

/**
 * Hàng thông tin dạng Key - Value
 */
@Composable
private fun DetailKeyValueRow(key: String, value: String) {
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
            modifier = Modifier.width(115.dp)
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
 * Nút hành động dạng viên thuốc gợi ý
 */
@Composable
private fun ActionSuggestPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .height(30.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
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
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
        }
    }
}

/**
 * Thanh điều hướng dưới cùng của màn hình phân tích chi tiết
 */
@Composable
private fun AiDetailBottomNavigationBar(
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
