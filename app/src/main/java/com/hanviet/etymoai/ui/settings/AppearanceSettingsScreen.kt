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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.ui.theme.ThemeModeOption
import com.hanviet.etymoai.ui.theme.ThemePreferences

/**
 * 3. Màn hình Giao diện & Hiển thị (Appearance, Accessibility & TTS Settings Screen)
 * Hiện thực hóa Mục 16.5 (Khả năng tiếp cận, cỡ chữ, định dạng Hán tự, giọng đọc TTS) của Idea 2.2.
 */
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit = {},
    onThemeChanged: (ThemeModeOption) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(ThemePreferences.getThemeMode(context)) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var selectedFontSizeIndex by remember { mutableIntStateOf(1) } // 0: Nhỏ, 1: Vừa, 2: Lớn, 3: Rất lớn
    var selectedHanjaFormat by remember { mutableStateOf("Hiển thị đầy đủ (Hán + Hán-Việt)") }
    var selectedTtsVoice by remember { mutableStateOf("🔊 Giọng nữ chuẩn Seoul (mặc định)") }

    val accentColors = listOf(
        Color(0xFF2563EB), // Xanh dương
        Color(0xFF7C3AED), // Tím
        Color(0xFF059669), // Xanh lục
        Color(0xFFEA580C), // Cam
        Color(0xFFDC2626)  // Đỏ
    )

    Scaffold(
        topBar = {
            AppearanceTopBar(onBackClick = onBackClick)
        },
        containerColor = Color(0xFFF4F7FB)
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
                Text(
                    text = "Chế độ giao diện",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 3 Cards: Sáng | Tối | Theo hệ thống
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeModeOption.entries.forEach { option ->
                        val isSelected = selectedTheme == option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedTheme = option
                                    onThemeChanged(option)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = option.titleVi,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF334155),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Card Tùy chọn hiển thị chi tiết
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Màu sắc chủ đạo
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Màu sắc chủ đạo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                accentColors.forEachIndexed { index, color ->
                                    val isSelected = selectedColorIndex == index
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable { selectedColorIndex = index }
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(2.5.dp, Color.White, CircleShape)
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Kích thước chữ
                        Column {
                            Text(
                                text = "Kích thước chữ (Accessibility)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Nhỏ", "Vừa", "Lớn", "Rất lớn").forEachIndexed { index, sizeLabel ->
                                    val isSelected = selectedFontSizeIndex == index
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedFontSizeIndex = index }
                                    ) {
                                        Text(
                                            text = sizeLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Ngôn ngữ giao diện
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Switch language */ },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ngôn ngữ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "🇻🇳 Tiếng Việt",
                                    fontSize = 13.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Định dạng Hán tự
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Hanja format selection */ },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Định dạng Hán",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = selectedHanjaFormat,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Cài đặt phát âm TTS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TTS voice selection */ },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cài đặt phát âm",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = selectedTtsVoice,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
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
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AppearanceTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = Color(0xFFF4F7FB),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Giao diện & Hiển thị",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}
