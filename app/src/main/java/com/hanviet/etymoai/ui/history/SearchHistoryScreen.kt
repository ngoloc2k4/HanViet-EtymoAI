package com.hanviet.etymoai.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.hanviet.etymoai.ui.theme.EtymoTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.OriginType

data class HistoryItem(
    val id: Long,
    val query: String,
    val phoneticVi: String,
    val originType: OriginType,
    val meaningSummary: String,
    val timeFormatted: String,
    val isFavorite: Boolean = false
)

/**
 * Màn hình Lịch sử Tra cứu & Sổ tay Từ vựng (SearchHistoryScreen)
 * Hiện thực hóa quy định tại Mục 4.1 Điều 8 & Mục 18 của Idea 2.2
 */
@Composable
fun SearchHistoryScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onClearAllHistory: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    var historyItems by remember {
        mutableStateOf(
            listOf(
                HistoryItem(1L, "도서관", "Đồ Thư Quán", OriginType.SINO_KOREAN, "Thư viện, nơi lưu trữ tài liệu", "Vừa xong", true),
                HistoryItem(2L, "한국어", "Hàn Quốc Ngữ", OriginType.SINO_KOREAN, "Tiếng Hàn, ngôn ngữ Hàn Quốc", "15 phút trước", true),
                HistoryItem(3L, "알바", "Arbeit", OriginType.LOANWORD, "Việc làm thêm, nghề phụ", "1 giờ trước", false),
                HistoryItem(4L, "선생님", "Tiên Sinh", OriginType.SINO_KOREAN, "Thầy giáo, cô giáo", "Hôm nay, 08:30", true),
                HistoryItem(5L, "나무", "Cây cối", OriginType.NATIVE_KOREAN, "Cây thân gỗ, gỗ xây dựng", "Hôm qua", false),
                HistoryItem(6L, "경제", "Kinh Tế", OriginType.SINO_KOREAN, "Nền kinh tế, hoạt động tài chính", "Hôm qua", false),
                HistoryItem(7L, "행복", "Hạnh Phúc", OriginType.SINO_KOREAN, "Trạng thái vui vẻ, hạnh phúc", "3 ngày trước", true),
                HistoryItem(8L, "커피", "Coffee", OriginType.LOANWORD, "Cà phê, thức uống từ hạt cà phê", "5 ngày trước", false)
            )
        )
    }

    val filteredItems = remember(searchQuery, historyItems) {
        if (searchQuery.isBlank()) historyItems
        else historyItems.filter {
            it.query.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.phoneticVi.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.meaningSummary.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            HistoryTopBar(
                onBackClick = onBackClick,
                hasItems = historyItems.isNotEmpty(),
                onClearAllClick = { showConfirmClearDialog = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search Box lọc lịch sử
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Tìm trong lịch sử tra cứu...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = EtymoTheme.divider()
                    ),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LỊCH SỬ TRA CỨU (${filteredItems.size} mục)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Text(
                        text = "Lưu trữ cục bộ • Riêng tư",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Chưa có lịch sử tra cứu" else "Không tìm thấy kết quả phù hợp",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { onItemClick(item.query) },
                        onToggleFavorite = {
                            historyItems = historyItems.map {
                                if (it.id == item.id) it.copy(isFavorite = !it.isFavorite) else it
                            }
                        },
                        onDelete = {
                            historyItems = historyItems.filterNot { it.id == item.id }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog xác nhận xóa sạch lịch sử
    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626))
            },
            title = {
                Text(text = "Xóa toàn bộ lịch sử?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    text = "Mọi từ khóa bạn đã tra cứu sẽ bị xóa sạch khỏi bộ nhớ máy theo tiêu chuẩn Quyền riêng tư (Mục 18). Hành động này không thể hoàn tác.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        historyItems = emptyList()
                        onClearAllHistory()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Xóa tất cả")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun HistoryTopBar(
    onBackClick: () -> Unit,
    hasItems: Boolean,
    onClearAllClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Lịch sử tra cứu",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (hasItems) {
                    TextButton(onClick = onClearAllClick) {
                        Text(text = "Xóa tất cả", fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeText = when (item.originType) {
        OriginType.SINO_KOREAN -> "Hán-Hàn"
        OriginType.NATIVE_KOREAN -> "Thuần Hàn"
        OriginType.LOANWORD -> "Ngoại lai"
        OriginType.MIXED -> "Hỗn hợp"
        else -> "Chưa xác định"
    }
    val badgeBg = EtymoTheme.originBadgeBg(item.originType.name)
    val badgeTextColor = EtymoTheme.originBadgeText(item.originType.name)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = EtymoTheme.cardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.query,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.timeFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${item.phoneticVi} • ${item.meaningSummary}",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Yêu thích",
                    tint = if (item.isFavorite) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa khỏi lịch sử",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
