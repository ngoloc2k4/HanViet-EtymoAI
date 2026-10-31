package com.hanviet.etymoai.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Màn hình Cài đặt Tổng quan (Settings Overview Screen / Hub Điều Hướng Cài Đặt)
 * Hiện thực hóa cấu trúc Danh mục điều hướng (Menu List) từ Wireframe Ảnh 1 - Trái
 */
@Composable
fun SettingsOverviewScreen(
    savedWordsCount: Int = 0,
    dueCardsCount: Int = 0,
    onBackClick: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToDataPack: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToSentenceAnalyzer: () -> Unit = {},
    onNavigateToStudyStats: () -> Unit = {},
    onNavigateToSrsDecks: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onOpenQuickLookupGuide: () -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            SettingsOverviewTopBar(onBackClick = onBackClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // 1. Thẻ Hồ sơ Học tập Offline
                SettingsProfileCard(
                    savedWordsCount = savedWordsCount,
                    dueCardsCount = dueCardsCount
                )
            }

            // 2. Nhóm: HỆ THỐNG & DỮ LIỆU
            item {
                SettingsGroupHeader(title = "HỆ THỐNG & DỮ LIỆU")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.Key,
                            iconTint = Color(0xFF2563EB),
                            title = "Tài khoản & Bảo mật (BYOK)",
                            subtitle = "Quản lý khóa API Gemini/OpenAI, Android Keystore",
                            onClick = onNavigateToSecurity
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFF1F5F9)
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.Storage,
                            iconTint = Color(0xFF0D9488),
                            title = "Quản lý dữ liệu (Data Pack)",
                            subtitle = "dictionary_core.db (52k từ, 6.5k Hán tự), Cập nhật SHA-256",
                            onClick = onNavigateToDataPack
                        )
                    }
                }
            }

            // 3. Nhóm: TÙY BIẾN GIAO DIỆN
            item {
                SettingsGroupHeader(title = "TÙY BIẾN GIAO DIỆN")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.Palette,
                            iconTint = Color(0xFF7C3AED),
                            title = "Giao diện & Hiển thị",
                            subtitle = "Chế độ Sáng/Tối, 5 màu chủ đạo, Cỡ chữ tiếp cận, TTS",
                            onClick = onNavigateToAppearance
                        )
                    }
                }
            }

            // 4. Nhóm: CÔNG CỤ HỌC TẬP & TRÍ TUỆ NHÂN TẠO
            item {
                SettingsGroupHeader(title = "CÔNG CỤ HỌC TẬP & TRÍ TUỆ NHÂN TẠO")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.AutoAwesome,
                            iconTint = Color(0xFF9333EA),
                            title = "AI Phân tích câu & Ngữ pháp",
                            subtitle = "Bóc tách cấu trúc câu, trợ từ và gốc từ nguyên Hán-Việt",
                            onClick = onNavigateToSentenceAnalyzer
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFF1F5F9)
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.School,
                            iconTint = Color(0xFF059669),
                            title = "Thống kê học tập SRS",
                            subtitle = "Tỷ lệ nhớ 68%, chuỗi ngày học, từ thường quên (Leech)",
                            onClick = onNavigateToStudyStats
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFF1F5F9)
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.BookmarkBorder,
                            iconTint = Color(0xFFEA580C),
                            title = "Quản lý bộ thẻ Flashcard",
                            subtitle = "Tổ chức flashcard theo chủ đề Hán-Hàn, TOPIK I/II",
                            onClick = onNavigateToSrsDecks
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFF1F5F9)
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.History,
                            iconTint = Color(0xFF475569),
                            title = "Lịch sử tra cứu & Ghi chú",
                            subtitle = "Nhật ký tìm kiếm offline, bảo mật riêng tư, xóa nhật ký",
                            onClick = onNavigateToHistory
                        )
                    }
                }
            }

            // 5. Nhóm: THÔNG TIN & TRỢ GIÚP
            item {
                SettingsGroupHeader(title = "THÔNG TIN & TRỢ GIÚP")
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.TextFields,
                            iconTint = Color(0xFF1E3A8A),
                            title = "Tra cứu toàn hệ thống (Quick Lookup)",
                            subtitle = "Kích hoạt tra từ bôi đen trong Chrome, KakaoTalk, PDF",
                            onClick = onOpenQuickLookupGuide
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFF1F5F9)
                        )
                        SettingsMenuItem(
                            icon = Icons.Default.Info,
                            iconTint = Color(0xFF3B82F6),
                            title = "Nguồn dữ liệu & Giấy phép bản quyền",
                            subtitle = "NIKL Quốc gia, Viện Hán Nôm, Cam kết Quyền riêng tư",
                            onClick = onNavigateToAbout
                        )
                    }
                }
            }

            item {
                // App footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HanViet-EtymoAI v1.0.0 (Beta)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kiến trúc Offline-First SQLite FTS5 • Bản quyền mở CC BY-SA",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar cho màn hình Cài đặt Tổng quan
 */
@Composable
private fun SettingsOverviewTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Cài đặt & Tùy chọn",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Thẻ Hồ sơ Không gian Học tập
 */
@Composable
private fun SettingsProfileCard(
    savedWordsCount: Int = 0,
    dueCardsCount: Int = 0
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "學", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Không gian học tập cá nhân",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$savedWordsCount từ vựng đã lưu • $dueCardsCount thẻ cần ôn hôm nay",
                    fontSize = 12.sp,
                    color = Color(0xFFBFDBFE)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Offline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Tiêu đề nhóm cài đặt
 */
@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

/**
 * Mục Menu Cài đặt đơn lẻ
 */
@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Bottom Navigation Bar với tab Cá nhân (index 4) được chọn
 */
@Composable
private fun SettingsOverviewBottomBar(
    onTabClick: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tra cứu", Icons.Default.Search, 1),
            Triple("AI Chat", Icons.Default.Psychology, 2),
            Triple("Từ vựng", Icons.Default.BookmarkBorder, 3),
            Triple("Cài đặt", Icons.Default.Person, 4)
        )

        navItems.forEach { (label, icon, index) ->
            val isSelected = index == 4

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabClick(index) },
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
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2563EB),
                    selectedTextColor = Color(0xFF2563EB),
                    indicatorColor = Color(0xFFDBEAFE),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                )
            )
        }
    }
}
