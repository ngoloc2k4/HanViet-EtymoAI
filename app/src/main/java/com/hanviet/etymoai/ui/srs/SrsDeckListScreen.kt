package com.hanviet.etymoai.ui.srs

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.srs.SrsRepository
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SrsDeckItem(
    val id: Long,
    val title: String,
    val description: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredPercentage: Float,
    val iconEmoji: String,
    val badgeColor: Color
)

/**
 * Màn hình Quản lý Danh sách Bộ thẻ Học tập SRS (SrsDeckListScreen)
 * Theo thiết kế Section 6 của DESIGN.md
 */
@Composable
fun SrsDeckListScreen(
    userWorkspaceRepository: UserWorkspaceRepository? = null,
    srsRepository: SrsRepository? = null,
    onBackClick: () -> Unit = {},
    onDeckClick: (SrsDeckItem) -> Unit = {},
    onStartReview: (SrsDeckItem) -> Unit = {}
) {
    var showCreateDeckDialog by remember { mutableStateOf(false) }
    var newDeckTitle by remember { mutableStateOf("") }
    var newDeckDesc by remember { mutableStateOf("") }
    var customDecks by remember { mutableStateOf(listOf<SrsDeckItem>()) }

    val savedWordsWithTags by (userWorkspaceRepository?.getAllSavedWordsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val dueCards by (srsRepository?.getDueCardsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val allCards by (srsRepository?.getAllCardsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val leechCards by (srsRepository?.getLeechCardsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val totalSavedCount = savedWordsWithTags.size
    val totalDueCount = dueCards.size
    val totalMastered = allCards.count { it.repetitions >= 3 || it.intervalDays >= 7 }
    val overallMastery = if (totalSavedCount > 0) totalMastered.toFloat() / totalSavedCount.toFloat() else 0f

    val sinoWords = savedWordsWithTags.filter { it.word.originType == "SINO_KOREAN" }
    val nativeWords = savedWordsWithTags.filter { it.word.originType == "NATIVE_KOREAN" }
    val loanWords = savedWordsWithTags.filter { it.word.originType == "LOANWORD" }
    val favoriteWords = savedWordsWithTags.filter { it.word.isFavorite }

    val builtInDecks = remember(savedWordsWithTags, dueCards, allCards, leechCards) {
        listOf(
            SrsDeckItem(
                id = 1L,
                title = "Tất cả từ vựng đã lưu",
                description = "Kho từ vựng cá nhân tổng hợp từ các bài tra cứu thực tế",
                totalCards = totalSavedCount,
                dueCards = totalDueCount,
                masteredPercentage = overallMastery,
                iconEmoji = "📚",
                badgeColor = Color(0xFF2563EB)
            ),
            SrsDeckItem(
                id = 2L,
                title = "Từ vựng Hán-Hàn cốt lõi (Sino-Korean)",
                description = "Gồm các danh từ gốc Hán ghép có cấu trúc chiết tự đa tầng",
                totalCards = sinoWords.size,
                dueCards = dueCards.count { it.word.originType == "SINO_KOREAN" },
                masteredPercentage = if (sinoWords.isNotEmpty()) (sinoWords.count { it.word.isFavorite }.toFloat() / sinoWords.size.toFloat()).coerceIn(0.1f, 1f) else 0f,
                iconEmoji = "漢",
                badgeColor = Color(0xFF1E3A8A)
            ),
            SrsDeckItem(
                id = 3L,
                title = "Từ vựng Yêu thích & Đánh dấu (★)",
                description = "Các mục từ quan trọng được bạn gắn dấu sao ưu tiên",
                totalCards = favoriteWords.size,
                dueCards = dueCards.count { it.word.isFavorite },
                masteredPercentage = if (favoriteWords.isNotEmpty()) 0.85f else 0f,
                iconEmoji = "⭐",
                badgeColor = Color(0xFFEAB308)
            ),
            SrsDeckItem(
                id = 4L,
                title = "Từ mượn & Ngoại lai (Loanwords)",
                description = "Các từ vay mượn từ tiếng Anh, Đức, Bồ Đào Nha",
                totalCards = loanWords.size,
                dueCards = dueCards.count { it.word.originType == "LOANWORD" },
                masteredPercentage = if (loanWords.isNotEmpty()) 0.70f else 0f,
                iconEmoji = "🌐",
                badgeColor = Color(0xFF0D9488)
            ),
            SrsDeckItem(
                id = 5L,
                title = "Từ ngữ Thuần Hàn sinh hoạt (Native)",
                description = "Từ vựng giao tiếp tự nhiên và động từ thuần Hàn cơ bản",
                totalCards = nativeWords.size,
                dueCards = dueCards.count { it.word.originType == "NATIVE_KOREAN" },
                masteredPercentage = if (nativeWords.isNotEmpty()) 0.65f else 0f,
                iconEmoji = "🌱",
                badgeColor = Color(0xFF16A34A)
            ),
            SrsDeckItem(
                id = 6L,
                title = "Từ thường quên (Leech words)",
                description = "Các mục từ đã quên ≥ 1 lần trong các chu kỳ ôn tập gần nhất",
                totalCards = leechCards.size,
                dueCards = leechCards.size,
                masteredPercentage = if (leechCards.isNotEmpty()) 0.20f else 1.0f,
                iconEmoji = "⚠️",
                badgeColor = Color(0xFFDC2626)
            )
        )
    }

    val decks = remember(builtInDecks, customDecks) {
        builtInDecks + customDecks
    }

    Scaffold(
        topBar = {
            SrsDeckTopBar(onBackClick = onBackClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDeckDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tạo bộ thẻ mới")
            }
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
                Spacer(modifier = Modifier.height(4.dp))
                // Banner tổng kết
                SrsDeckSummaryBanner(
                    totalDecks = decks.size,
                    totalCards = decks.sumOf { it.totalCards },
                    totalDue = decks.sumOf { it.dueCards }
                )
            }

            item {
                Text(
                    text = "DANH SÁCH BỘ THẺ ÔN TẬP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            items(decks) { deck ->
                DeckItemCard(
                    deck = deck,
                    onClick = { onDeckClick(deck) },
                    onStartReview = { onStartReview(deck) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Dialog tạo bộ thẻ mới
    if (showCreateDeckDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDeckDialog = false },
            title = {
                Text(text = "Tạo bộ thẻ ôn tập mới", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newDeckTitle,
                        onValueChange = { newDeckTitle = it },
                        label = { Text("Tên bộ thẻ") },
                        placeholder = { Text("Ví dụ: Từ vựng TOPIK II") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDeckDesc,
                        onValueChange = { newDeckDesc = it },
                        label = { Text("Mô tả / Mục tiêu") },
                        placeholder = { Text("Ví dụ: 50 từ Hán-Hàn cao cấp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDeckTitle.isNotBlank()) {
                            customDecks = customDecks + SrsDeckItem(
                                id = System.currentTimeMillis(),
                                title = newDeckTitle.trim(),
                                description = newDeckDesc.ifBlank { "Bộ thẻ người dùng tự định nghĩa" },
                                totalCards = 0,
                                dueCards = 0,
                                masteredPercentage = 0f,
                                iconEmoji = "📁",
                                badgeColor = Color(0xFF4F46E5)
                            )
                            newDeckTitle = ""
                            newDeckDesc = ""
                        }
                        showCreateDeckDialog = false
                    }
                ) {
                    Text("Tạo bộ thẻ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDeckDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun SrsDeckTopBar(onBackClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
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
                text = "Bộ thẻ ghi nhớ (SRS)",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SrsDeckSummaryBanner(
    totalDecks: Int,
    totalCards: Int,
    totalDue: Int
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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalDecks", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Bộ thẻ", fontSize = 11.5.sp, color = Color(0xFFBFDBFE))
            }
            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFF3B82F6)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalCards", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Tổng số thẻ", fontSize = 11.5.sp, color = Color(0xFFBFDBFE))
            }
            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFF3B82F6)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalDue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                Text(text = "Cần ôn hôm nay", fontSize = 11.5.sp, color = Color(0xFFBFDBFE))
            }
        }
    }
}

@Composable
private fun DeckItemCard(
    deck: SrsDeckItem,
    onClick: () -> Unit,
    onStartReview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(deck.badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = deck.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = deck.description,
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )
                }

                if (deck.dueCards > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "${deck.dueCards} thẻ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thanh tiến độ ghi nhớ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { deck.masteredPercentage },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = deck.badgeColor,
                    trackColor = Color(0xFFF1F5F9)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${(deck.masteredPercentage * 100).toInt()}% thuộc",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deck.totalCards} thẻ tổng cộng",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Button(
                    onClick = onStartReview,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = deck.badgeColor),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Ôn tập ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
