package com.hanviet.etymoai.ui.home

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Work
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
import com.hanviet.etymoai.ui.components.DatabaseStatus
import com.hanviet.etymoai.ui.components.KoreanJamoKeyboardBottomSheet
import com.hanviet.etymoai.ui.components.OfflineStatusPill
import com.hanviet.etymoai.ui.components.VoiceSearchOverlay
import com.hanviet.etymoai.ui.components.rememberBounceScale
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.data.db.workspace.entity.SearchHistoryEntity
import com.hanviet.etymoai.data.translation.TranslationEngine
import com.hanviet.etymoai.data.translation.TranslationResult
import com.hanviet.etymoai.domain.search.SearchResult
import kotlinx.coroutines.launch

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
    recentSearches: List<SearchHistoryEntity> = emptyList(),
    onClearRecentSearches: () -> Unit = {},
    onTranslate: (suspend (String, TranslationEngine, String, String) -> Result<TranslationResult>)? = null,
    onNavigateToSentenceAnalyzer: (String) -> Unit = {},
    onNavigateToDictionary: () -> Unit = {},
    onNavigateToHanja: () -> Unit = {},
    onNavigateToMorphology: () -> Unit = {},
    onNavigateToAi: () -> Unit = {},
    onNavigateToSrs: () -> Unit = {},
    onNavigateToQuickLookupGuide: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    dueCardsCount: Int = 0,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onDeleteRecentSearch: (Long) -> Unit = {},
    onBookmarkWord: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showJamoKeyboard by remember { mutableStateOf(false) }
    var showVoiceSearch by remember { mutableStateOf(false) }
    var showDbInfoDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
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

                            // Huy hiệu Offline ([● Offline] Status Pill)
                            OfflineStatusPill(
                                status = DatabaseStatus.READY,
                                versionLabel = "v1.2.0",
                                onClick = { showDbInfoDialog = true }
                            )
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
                        onOpenJamoKeyboard = { showJamoKeyboard = true },
                        onOpenVoiceSearch = { showVoiceSearch = true },
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
                            badgeCount = dueCardsCount,
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

            // ================= 5. KHU VỰC DỊCH THUẬT NHANH (MÁY DỊCH & AI) =================
            item {
                Spacer(modifier = Modifier.height(16.dp))
                QuickTranslationCard(
                    onTranslate = onTranslate,
                    onNavigateToSentenceAnalyzer = onNavigateToSentenceAnalyzer
                )
            }

            // ================= 6. KHỐI TRA CỨU GẦN ĐÂY (RECENT SEARCHES THỰC TẾ) =================
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (recentSearches.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onClearRecentSearches() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Xóa lịch sử",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Xóa",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onNavigateToHistory() }
                                ) {
                                    Text(
                                        text = "Xem tất cả",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2563EB)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        if (recentSearches.isEmpty()) {
                            // Khi chưa có lịch sử, thông báo tinh tế
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🌱", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Chưa có lịch sử tra cứu gần đây.\nBắt đầu gõ từ bất kỳ để khám phá từ nguyên!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        } else {
                            // Danh sách các từ tra cứu thực tế từ search_history
                            recentSearches.take(5).forEachIndexed { index, item ->
                                RealSearchHistoryRow(
                                    item = item,
                                    onClick = { onWordClick(item.queryText) },
                                    onDelete = { onDeleteRecentSearch(item.id) },
                                    onBookmark = { onBookmarkWord(item.queryText) }
                                )
                                if (index < minOf(recentSearches.size, 5) - 1) {
                                    HorizontalDivider(
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.padding(start = 44.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= 7. THẺ KHÁM PHÁ (EXPLORE PROMO BANNER) =================
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
                        .clickable { onNavigateToAbout() }
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

    if (showJamoKeyboard) {
        KoreanJamoKeyboardBottomSheet(
            currentText = searchQuery,
            onTextChange = onSearchQueryChange,
            onDismissRequest = { showJamoKeyboard = false }
        )
    }

    if (showVoiceSearch) {
        VoiceSearchOverlay(
            onDismissRequest = { showVoiceSearch = false },
            onVoiceResult = { result ->
                onSearchQueryChange(result)
                onWordClick(result)
            }
        )
    }

    if (showDbInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDbInfoDialog = false },
            title = {
                Text(
                    text = "Gói Dữ Liệu Offline NIKL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "• Phiên bản: KRDICT v1.2.0 (Chính thức)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        text = "• 52.000+ mục từ tiếng Hàn – Việt",
                        fontSize = 12.5.sp,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "• 6.500+ chữ Hán chiết tự (Hanja master)",
                        fontSize = 12.5.sp,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "• Tìm kiếm FTS5 tức thì 0ms (Không cần internet)",
                        fontSize = 12.5.sp,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "• Giấy phép: CC BY-SA 2.0 KR (Viện Quốc ngữ Hàn Quốc)",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDbInfoDialog = false }) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}

/**
 * Hàng mục tra cứu gần đây thực tế (Real Search History Row)
 */
@Composable
private fun RealSearchHistoryRow(
    item: SearchHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onBookmark: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        // Icon ngữ cảnh tương ứng
        val contextIcon = when {
            item.queryText.any { it.code in 0x4E00..0x9FFF } -> "漢"
            item.queryText.contains(" ") -> "📄"
            item.queryText.any { it in 'a'..'z' || it in 'A'..'Z' } -> "🔗"
            else -> "📖"
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF))
        ) {
            Text(text = contextIcon, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Từ vựng và Nghĩa
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.queryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            val resultNote = if (item.resultCount > 0) "${item.resultCount} kết quả" else "Tra cứu gần đây"
            Text(
                text = resultNote,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B)
            )
        }

        // Nút Lưu nhanh (Bookmark)
        IconButton(
            onClick = onBookmark,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Lưu từ",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(16.dp)
            )
        }

        // Nút Xóa (Delete)
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Xóa",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }

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
 * Thẻ Dịch thuật Nhanh (Quick Translation Card)
 * Hỗ trợ chọn dịch bằng Máy dịch (Google Translate/DeepL) hoặc AI Grounded (BYOK)
 */
@Composable
private fun QuickTranslationCard(
    onTranslate: (suspend (String, TranslationEngine, String, String) -> Result<TranslationResult>)?,
    onNavigateToSentenceAnalyzer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var selectedEngine by remember { mutableStateOf(TranslationEngine.GOOGLE_TRANSLATE) }
    var isKoToVi by remember { mutableStateOf(true) } // true: KO -> VI, false: VI -> KO
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Language Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dịch thuật nhanh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (isKoToVi) "Tiếng Hàn ➔ Tiếng Việt" else "Tiếng Việt ➔ Tiếng Hàn",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Switch language direction button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clickable { isKoToVi = !isKoToVi }
                        .padding(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Đổi hướng dịch",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isKoToVi) "KO ⇄ VI" else "VI ⇄ KO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Engine Selector Tabs: Máy dịch vs AI Grounded
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val engines = listOf(
                    TranslationEngine.GOOGLE_TRANSLATE to "Máy dịch (Google/DeepL)",
                    TranslationEngine.AI_GROUNDED to "AI Trợ lý (BYOK)"
                )
                engines.forEach { (engine, label) ->
                    val isSelected = selectedEngine == engine
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clickable { selectedEngine = engine }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Input Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    BasicTextField(
                        value = sourceText,
                        onValueChange = {
                            sourceText = it
                            errorMessage = null
                        },
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A),
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF2563EB)),
                        decorationBox = { innerTextField ->
                            if (sourceText.isEmpty()) {
                                Text(
                                    text = if (isKoToVi) "Nhập câu tiếng Hàn cần dịch..." else "Nhập câu tiếng Việt cần dịch sang tiếng Hàn...",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 55.dp, max = 120.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (sourceText.isNotEmpty()) {
                            IconButton(
                                onClick = { sourceText = ""; translatedText = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xoá",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (sourceText.isBlank()) return@Button
                                isTranslating = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val src = if (isKoToVi) "ko" else "vi"
                                    val tgt = if (isKoToVi) "vi" else "ko"
                                    val result = onTranslate?.invoke(sourceText.trim(), selectedEngine, src, tgt)
                                    if (result != null && result.isSuccess) {
                                        translatedText = result.getOrNull()?.translatedText ?: ""
                                    } else {
                                        errorMessage = result?.exceptionOrNull()?.message ?: "Không thể kết nối máy dịch."
                                    }
                                    isTranslating = false
                                }
                            },
                            enabled = sourceText.isNotBlank() && !isTranslating,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isTranslating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text("Dịch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Error or Result Display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    fontSize = 11.5.sp,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (translatedText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bản dịch:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D4ED8)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(translatedText))
                                    Toast.makeText(context, "Đã sao chép bản dịch", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Sao chép",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = translatedText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    val textToAnalyze = if (isKoToVi) sourceText else translatedText
                                    onNavigateToSentenceAnalyzer(textToAnalyze)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Phân tích ngữ pháp",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }
                }
            }
        }
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
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale = rememberBounceScale(interactionSource)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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
                Box(modifier = Modifier.size(34.dp)) {
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

                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
    onOpenJamoKeyboard: () -> Unit = {},
    onOpenVoiceSearch: () -> Unit = {},
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
                .padding(horizontal = 14.dp)
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

            // Nút Bàn phím Jamo Hàn Quốc
            IconButton(
                onClick = onOpenJamoKeyboard,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Bàn phím Jamo tiếng Hàn",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Nút Micro tìm kiếm giọng nói
            IconButton(
                onClick = onOpenVoiceSearch,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Tìm kiếm bằng giọng nói",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
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
            Triple("AI Chat", Icons.Default.Psychology, 2),
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
