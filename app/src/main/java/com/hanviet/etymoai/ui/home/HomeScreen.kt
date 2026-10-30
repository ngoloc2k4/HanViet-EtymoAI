package com.hanviet.etymoai.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.search.SearchResult

/**
 * Mục tìm kiếm gần đây mẫu trên màn hình chính
 */
data class RecentSearchItem(
    val id: Long,
    val headword: String,
    val meaning: String,
    val badgeLabel: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val iconVector: ImageVector? = null,
    val hanjaChar: String? = null
)

/**
 * Màn hình Chính (Homepage) mô phỏng chính xác bản phác thảo SVG với thẩm mỹ Oriental Modernism.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    searchResults: List<SearchResult> = emptyList(),
    onSearchResultClick: (SearchResult) -> Unit = {},
    onWordClick: (String) -> Unit = {},
    onNavigateToDictionary: () -> Unit = {},
    onNavigateToHanja: () -> Unit = {},
    onNavigateToMorphology: () -> Unit = {},
    onNavigateToAi: () -> Unit = {},
    onNavigateToSrs: () -> Unit = {},
    onNavigateToQuickLookupGuide: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    // Danh sách 5 mục tra cứu gần đây chính xác như trong SVG mockup
    val recentSearches = remember {
        listOf(
            RecentSearchItem(
                id = 1L,
                headword = "도서관",
                meaning = "thư viện",
                badgeLabel = "Hán-Hàn",
                badgeBgColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF2563EB),
                iconVector = Icons.Default.MenuBook
            ),
            RecentSearchItem(
                id = 2L,
                headword = "들었어요",
                meaning = "(đã) nghe, nghe được",
                badgeLabel = "Động từ",
                badgeBgColor = Color(0xFFECFDF5),
                badgeTextColor = Color(0xFF059669),
                iconVector = Icons.Default.AccountTree
            ),
            RecentSearchItem(
                id = 3L,
                headword = "행복",
                meaning = "hạnh phúc",
                badgeLabel = "Hán-Hàn",
                badgeBgColor = Color(0xFFEEF2FF),
                badgeTextColor = Color(0xFF4F46E5),
                hanjaChar = "漢"
            ),
            RecentSearchItem(
                id = 4L,
                headword = "아르바이트",
                meaning = "làm thêm, việc làm thêm",
                badgeLabel = "Ngoại lai",
                badgeBgColor = Color(0xFFFFF1F2),
                badgeTextColor = Color(0xFFE11D48),
                iconVector = Icons.Default.Work
            ),
            RecentSearchItem(
                id = 5L,
                headword = "사랑",
                meaning = "tình yêu",
                badgeLabel = "Thuần Hàn",
                badgeBgColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF2563EB),
                iconVector = Icons.Default.AutoAwesome
            )
        )
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    selectedTab = tabIndex
                    when (tabIndex) {
                        1 -> onNavigateToDictionary()
                        2 -> onNavigateToSrs()
                        3 -> onOpenSettings()
                    }
                }
            )
        },
        containerColor = Color(0xFFF4F7FB),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ================= 1. HERO BANNER PHONG CẢNH & HEADER =================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    // Vẽ đồ họa phong cảnh núi mờ, mái đình cổ Gyeongbokgung và hoa anh đào
                    HeroLandscapeCanvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    // Thanh tiêu đề thương hiệu & trạng thái Offline
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Logo Hoa sen / Ngọn lửa tri thức
                            LotusFlameLogo(modifier = Modifier.size(36.dp))

                            Spacer(modifier = Modifier.width(10.dp))

                            // Tên ứng dụng
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "HanViet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "EtymoAI",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Nút Cài đặt (Bánh răng)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                                    .clickable { onOpenSettings() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Cài đặt",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Huy hiệu Offline (xanh lá nhạt)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Offline",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Slogan triết lý
                        Text(
                            text = "Hiểu từ gốc • Giỏi ngôn ngữ • Kết nối văn hóa",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(start = 46.dp)
                        )
                    }

                    // Thanh tìm kiếm nổi (Floating Search Bar)
                    FloatingSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearchSubmit = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotBlank()) {
                                onWordClick(searchQuery)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // ================= 2. KẾT QUẢ TÌM KIẾM TRỰC TIẾP (NẾU ĐANG NHẬP) =================
            if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Kết quả tìm kiếm cho \"$searchQuery\":",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            searchResults.take(6).forEach { result ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSearchResultClick(result) }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = result.headwordNfc,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (!result.phoneticVi.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${result.phoneticVi})",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF2563EB)
                                                )
                                            }
                                        }
                                        Text(
                                            text = result.primaryMeaningVi,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }

            // ================= 3. DÃY CHIP GỢI Ý NHANH (SUGGESTION CHIPS) =================
            item {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val chips = listOf(
                        "✦ 도서관",
                        "한자",
                        "들었어요",
                        "아르바이트",
                        "nghĩa, từ gốc"
                    )
                    items(chips) { chipText ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .height(26.dp)
                                .clickable {
                                    val cleaned = chipText.replace("✦ ", "").trim()
                                    onSearchQueryChange(cleaned)
                                    onWordClick(cleaned)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                if (chipText.startsWith("✦")) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "✦ ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                        Text(
                                            text = chipText.substring(2),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = chipText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= 4. LƯỚI 6 THẺ TÍNH NĂNG (2 HÀNG x 3 CỘT) =================
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hàng 1 (Cards 1, 2, 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCard(
                            title = "Tra cứu từ điển",
                            subtitle = "Nhanh chóng, chính xác",
                            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                            iconVector = Icons.Default.Search,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDictionary
                        )
                        FeatureCard(
                            title = "Hán tự & Hán-Việt",
                            subtitle = "Giải thích chi tiết",
                            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                            hanjaText = "漢",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToHanja
                        )
                        FeatureCard(
                            title = "Phân tích từ gốc",
                            subtitle = "Hình thái học thông minh",
                            gradientColors = listOf(Color(0xFF34D399), Color(0xFF10B981)),
                            iconVector = Icons.Default.AccountTree,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMorphology
                        )
                    }

                    // Hàng 2 (Cards 4, 5, 6)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCard(
                            title = "AI hỗ trợ",
                            subtitle = "Giải thích theo ngữ cảnh",
                            gradientColors = listOf(Color(0xFFFB7185), Color(0xFFF43F5E)),
                            iconVector = Icons.Default.Psychology,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAi
                        )
                        FeatureCard(
                            title = "Từ vựng của tôi",
                            subtitle = "Lưu & ôn tập (SRS)",
                            gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                            iconVector = Icons.Default.Bookmark,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSrs
                        )
                        FeatureCard(
                            title = "Tra cứu nhanh",
                            subtitle = "Từ màn hình khác",
                            gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                            iconVector = Icons.Default.OpenInNew,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToQuickLookupGuide
                        )
                    }
                }
            }

            // ================= 5. KHỐI TRA CỨU GẦN ĐÂY (RECENT SEARCHES) =================
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tiêu đề khối
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tra cứu gần đây",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Xem tất cả  ›",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable { onNavigateToDictionary() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Danh sách 5 mục
                        recentSearches.forEachIndexed { index, item ->
                            RecentSearchRow(
                                item = item,
                                onClick = { onWordClick(item.headword) }
                            )
                            if (index < recentSearches.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(start = 44.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ================= 6. THẺ KHÁM PHÁ (EXPLORE PROMO BANNER) =================
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToAi() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE0EFFE), Color(0xFFE8F1FF))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Icon ngôi sao phát sáng
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Text(
                                    text = "✦",
                                    fontSize = 15.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Khám phá HanViet EtymoAI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Từ điển thông minh • Offline-first • Mã nguồn mở",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }

                            Text(
                                text = "›",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hàng mục tra cứu gần đây (Recent Search Row)
 */
@Composable
private fun RecentSearchRow(
    item: RecentSearchItem,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        // Icon hình tròn tròn nhỏ
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(item.badgeBgColor)
        ) {
            if (item.hanjaChar != null) {
                Text(
                    text = item.hanjaChar,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = item.badgeTextColor
                )
            } else if (item.iconVector != null) {
                Icon(
                    imageVector = item.iconVector,
                    contentDescription = null,
                    tint = item.badgeTextColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Từ vựng và Nghĩa
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.headword,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = item.meaning,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        // Huy hiệu phân loại (Badge)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = item.badgeBgColor,
            modifier = Modifier.height(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.badgeTextColor
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Mũi tên ›
        Text(
            text = "›",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
    }
}

/**
 * Thẻ tính năng (Feature Card - 1 trong 6 thẻ)
 */
@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    hanjaText: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Container chứa Icon với Gradient
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(gradientColors))
                ) {
                    if (hanjaText != null) {
                        Text(
                            text = hanjaText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )
                    } else if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mũi tên nhỏ ở góc trên
                Text(
                    text = "›",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tiêu đề tính năng
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Phụ đề mô tả
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Thanh tìm kiếm nổi (Floating Search Bar)
 */
@Composable
private fun FloatingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Icon Kính lúp
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Ô nhập văn bản
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Nhập từ, câu tiếng Hàn, Hán-Việt hoặc tiếng Việt...",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                    cursorBrush = SolidColor(Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Nút xoá nếu có nội dung
            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xoá",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Icon Bàn phím
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Bàn phím",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Icon Micro thu âm (màu xanh dương đậm)
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Giọng nói",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Thanh điều hướng dưới cùng (Bottom Navigation Bar)
 */
@Composable
private fun HomeBottomNavigationBar(
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
            Triple("Từ vựng", Icons.Default.BookmarkBorder, 2),
            Triple("Cá nhân", Icons.Default.Person, 3)
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

/**
 * Logo Hoa sen & Ngọn lửa tri thức cách điệu bằng Compose Canvas
 */
@Composable
private fun LotusFlameLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Cánh xanh ngoài trái
        val leftPetal = Path().apply {
            moveTo(w * 0.45f, h * 0.95f)
            cubicTo(w * 0.1f, h * 0.85f, 0f, h * 0.55f, w * 0.1f, h * 0.25f)
            cubicTo(w * 0.2f, h * 0.4f, w * 0.35f, h * 0.55f, w * 0.45f, h * 0.75f)
            close()
        }
        drawPath(leftPetal, Color(0xFF1E3A8A))

        // Cánh xanh ngoài phải
        val rightPetal = Path().apply {
            moveTo(w * 0.55f, h * 0.95f)
            cubicTo(w * 0.9f, h * 0.85f, w, h * 0.55f, w * 0.9f, h * 0.25f)
            cubicTo(w * 0.8f, h * 0.4f, w * 0.65f, h * 0.55f, w * 0.55f, h * 0.75f)
            close()
        }
        drawPath(rightPetal, Color(0xFF1D4ED8))

        // Cánh xanh lam giữa
        val centerPetal = Path().apply {
            moveTo(w * 0.3f, h * 0.75f)
            cubicTo(w * 0.2f, h * 0.5f, w * 0.3f, h * 0.25f, w * 0.5f, h * 0.05f)
            cubicTo(w * 0.45f, h * 0.3f, w * 0.4f, h * 0.55f, w * 0.5f, h * 0.75f)
            close()
        }
        drawPath(centerPetal, Color(0xFF2563EB))

        // Ngọn lửa tri thức cam/đỏ bên trong
        val flameOuter = Path().apply {
            moveTo(w * 0.5f, h * 0.75f)
            cubicTo(w * 0.35f, h * 0.55f, w * 0.4f, h * 0.3f, w * 0.55f, h * 0.12f)
            cubicTo(w * 0.65f, h * 0.3f, w * 0.75f, h * 0.5f, w * 0.55f, h * 0.75f)
            close()
        }
        drawPath(flameOuter, Color(0xFFF97316))

        val flameCore = Path().apply {
            moveTo(w * 0.5f, h * 0.75f)
            cubicTo(w * 0.42f, h * 0.6f, w * 0.45f, h * 0.45f, w * 0.55f, h * 0.32f)
            cubicTo(w * 0.62f, h * 0.45f, w * 0.62f, h * 0.6f, w * 0.52f, h * 0.75f)
            close()
        }
        drawPath(flameCore, Color(0xFFEF4444))
    }
}

/**
 * Đồ họa Canvas phong cảnh núi mờ Á Đông, đình Gyeongbokgung cổ và cành hoa anh đào
 */
@Composable
private fun HeroLandscapeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Nền chuyển sắc bầu trời
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF), Color(0xFFDBEAFE))
            ),
            size = size
        )

        // 2. Núi xa (Mountain Far)
        val mountainFarPath = Path().apply {
            moveTo(0f, h * 0.78f)
            lineTo(w * 0.15f, h * 0.61f)
            lineTo(w * 0.28f, h * 0.70f)
            lineTo(w * 0.45f, h * 0.52f)
            lineTo(w * 0.60f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.48f)
            lineTo(w * 0.90f, h * 0.62f)
            lineTo(w, h * 0.55f)
            lineTo(w, h * 0.88f)
            lineTo(0f, h * 0.88f)
            close()
        }
        drawPath(
            path = mountainFarPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF93C5FD).copy(alpha = 0.55f), Color(0xFFBFDBFE).copy(alpha = 0.15f))
            )
        )

        // 3. Núi trung cảnh (Mountain Mid)
        val mountainMidPath = Path().apply {
            moveTo(w * 0.25f, h * 0.82f)
            lineTo(w * 0.42f, h * 0.63f)
            lineTo(w * 0.56f, h * 0.74f)
            lineTo(w * 0.70f, h * 0.56f)
            lineTo(w * 0.86f, h * 0.71f)
            lineTo(w, h * 0.60f)
            lineTo(w, h * 0.89f)
            lineTo(w * 0.25f, h * 0.89f)
            close()
        }
        drawPath(
            path = mountainMidPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF60A5FA).copy(alpha = 0.55f), Color(0xFF93C5FD).copy(alpha = 0.25f))
            )
        )

        // 4. Bóng các tòa nhà xa xôi
        val buildingColor = Color(0xFFCBD5E1).copy(alpha = 0.65f)
        drawRect(buildingColor, topLeft = Offset(w * 0.55f, h * 0.72f), size = Size(w * 0.015f, h * 0.08f))
        drawRect(buildingColor, topLeft = Offset(w * 0.57f, h * 0.70f), size = Size(w * 0.020f, h * 0.10f))
        drawRect(buildingColor, topLeft = Offset(w * 0.60f, h * 0.73f), size = Size(w * 0.012f, h * 0.07f))
        drawRect(buildingColor, topLeft = Offset(w * 0.63f, h * 0.71f), size = Size(w * 0.018f, h * 0.09f))

        // 5. Mặt hồ nước gợn sóng
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFDBEAFE).copy(alpha = 0.5f),
                    Color(0xFF93C5FD).copy(alpha = 0.75f),
                    Color(0xFF60A5FA).copy(alpha = 0.5f)
                )
            ),
            topLeft = Offset(0f, h * 0.80f),
            size = Size(w, h * 0.13f)
        )

        // 6. Mái đình cổ Hàn Quốc (Gyeongbokgung Pavilion)
        val pavilionX = w * 0.72f
        val pavilionY = h * 0.55f

        // Cột và tầng dưới
        drawRect(Color(0xFF334155), topLeft = Offset(pavilionX, pavilionY + h * 0.12f), size = Size(w * 0.15f, h * 0.08f))
        // Mái tầng 1 cong vút
        val roofTier1 = Path().apply {
            moveTo(pavilionX - w * 0.04f, pavilionY + h * 0.12f)
            quadraticTo(pavilionX + w * 0.075f, pavilionY + h * 0.09f, pavilionX + w * 0.19f, pavilionY + h * 0.12f)
            lineTo(pavilionX + w * 0.17f, pavilionY + h * 0.08f)
            quadraticTo(pavilionX + w * 0.075f, pavilionY + h * 0.095f, pavilionX - w * 0.02f, pavilionY + h * 0.08f)
            close()
        }
        drawPath(roofTier1, Color(0xFF1E293B))

        // Mái tầng 2 đỉnh nhọn
        val roofTier2 = Path().apply {
            moveTo(pavilionX - w * 0.01f, pavilionY + h * 0.08f)
            quadraticTo(pavilionX + w * 0.075f, pavilionY + h * 0.05f, pavilionX + w * 0.16f, pavilionY + h * 0.08f)
            lineTo(pavilionX + w * 0.13f, pavilionY + h * 0.03f)
            lineTo(pavilionX + w * 0.075f, pavilionY)
            lineTo(pavilionX + w * 0.02f, pavilionY + h * 0.03f)
            close()
        }
        drawPath(roofTier2, Color(0xFF0F172A))

        // 7. Cành hoa anh đào (Cherry Blossoms) góc phải trên
        val branchPath = Path().apply {
            moveTo(w * 0.98f, h * 0.05f)
            quadraticTo(w * 0.90f, h * 0.20f, w * 0.82f, h * 0.40f)
            moveTo(w * 0.93f, h * 0.15f)
            quadraticTo(w * 0.85f, h * 0.22f, w * 0.78f, h * 0.30f)
        }
        drawPath(
            path = branchPath,
            color = Color(0xFF5C4033),
            style = Stroke(width = 2.5f)
        )

        // Các đốm cánh hoa anh đào hồng thắm
        val petals = listOf(
            Pair(Offset(w * 0.95f, h * 0.10f), Color(0xFFFBCFE8)),
            Pair(Offset(w * 0.91f, h * 0.16f), Color(0xFFF9A8D4)),
            Pair(Offset(w * 0.96f, h * 0.18f), Color(0xFFFCE7F3)),
            Pair(Offset(w * 0.88f, h * 0.23f), Color(0xFFFBCFE8)),
            Pair(Offset(w * 0.92f, h * 0.25f), Color(0xFFF472B6)),
            Pair(Offset(w * 0.82f, h * 0.27f), Color(0xFFFCE7F3)),
            Pair(Offset(w * 0.86f, h * 0.32f), Color(0xFFFBCFE8)),
            Pair(Offset(w * 0.93f, h * 0.34f), Color(0xFFF9A8D4)),
            Pair(Offset(w * 0.84f, h * 0.38f), Color(0xFFFBCFE8))
        )
        petals.forEach { (pos, color) ->
            drawCircle(color = color, radius = 7f, center = pos)
        }
    }
}
