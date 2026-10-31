package com.hanviet.etymoai.ui.srs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.srs.SrsRepository
import com.hanviet.etymoai.ui.components.rememberBounceScale
import com.hanviet.etymoai.ui.theme.EtymoTheme
import kotlinx.coroutines.flow.flowOf

data class LeechWordItem(
    val rank: Int,
    val headword: String,
    val posLabel: String,
    val lastReviewedDaysAgo: String,
    val lapsesCount: Int = 3
)

/**
 * Màn hình Thống Kê Học Tập & Từ Thường Quên (Study Progress & Leech Words Screen - UX/UI v2.2)
 * Đặc tả Mục 12:
 * - 4 Metric Cards: Tổng lưu | Đã học | Đã thuộc | Chuỗi ngày
 * - Biểu đồ Donut Progress Chart với hiệu ứng hoạt họa quét (0% -> 68% trong 500ms)
 * - Biểu đồ cột 7 ngày trong tuần với cột nổi bật
 * - Timeframe chips: 7 ngày, 30 ngày, 3 tháng, Tùy chỉnh
 * - Bảng xếp hạng từ thường quên (Leech words) với huy hiệu (1)-(5) và lối tắt xem từ
 */
@Composable
fun StudyStatsScreen(
    userWorkspaceRepository: UserWorkspaceRepository? = null,
    srsRepository: SrsRepository? = null,
    onBackClick: () -> Unit = {},
    onWordClick: (String) -> Unit = {}
) {
    var selectedTimeframe by remember { mutableStateOf("7 ngày") }
    val haptic = LocalHapticFeedback.current

    // Lấy số liệu thực tế từ workspace repository và SRS repository
    val savedWordsWithTags by (userWorkspaceRepository?.getAllSavedWordsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val allSrsCards by (srsRepository?.getAllCardsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val realLeechWords by (srsRepository?.getLeechCardsFlow(10) ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val totalSaved = if (savedWordsWithTags.isNotEmpty()) savedWordsWithTags.size else allSrsCards.size.coerceAtLeast(0)
    val masteredCount = if (allSrsCards.isNotEmpty()) {
        allSrsCards.count { it.repetitions >= 3 || it.intervalDays >= 7 }
    } else {
        savedWordsWithTags.count { it.word.isFavorite }
    }
    val learningCount = if (allSrsCards.isNotEmpty()) {
        allSrsCards.count { it.repetitions in 1..2 }
    } else {
        (totalSaved - masteredCount).coerceAtLeast(0)
    }
    val masteryRatio = if (totalSaved > 0) masteredCount.toFloat() / totalSaved.toFloat() else 0f

    // Animated Sweep cho Donut Chart
    var chartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        chartVisible = true
    }

    val animatedSweep by animateFloatAsState(
        targetValue = if (chartVisible) masteryRatio.coerceIn(0.05f, 1f) else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "donutSweep"
    )

    val leechWords = remember(realLeechWords) {
        realLeechWords.map {
            LeechWordItem(
                rank = it.rank,
                headword = it.headword,
                posLabel = when (it.posLabel) {
                    "SINO_KOREAN" -> "Hán-Hàn"
                    "NATIVE_KOREAN" -> "Thuần Hàn"
                    "LOANWORD" -> "Ngoại lai"
                    else -> "Từ vựng"
                },
                lastReviewedDaysAgo = it.lastReviewedDaysAgo,
                lapsesCount = it.lapsesCount
            )
        }
    }

    Scaffold(
        topBar = {
            StudyStatsTopBar(onBackClick = onBackClick)
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
                Spacer(modifier = Modifier.height(2.dp))
                // 1. Timeframe Filter Chips
                TimeframeChipsRow(
                    selected = selectedTimeframe,
                    onSelected = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTimeframe = it
                    }
                )
            }

            item {
                // 2. 4 Metric Cards Grid: Tổng lưu | Đã học | Đã thuộc | Chuỗi ngày
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        icon = "📖",
                        title = "Tổng lưu",
                        value = "$totalSaved",
                        valueColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = "🎓",
                        title = "Đang học",
                        value = "$learningCount",
                        valueColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = "🛡️",
                        title = "Đã thuộc",
                        value = "$masteredCount",
                        valueColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = "🔥",
                        title = "Chuỗi ngày",
                        value = "7",
                        valueColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Tiến độ học tập",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                )
            }

            item {
                // 3. Hai thẻ Tiến độ: Donut Gauge + Biểu đồ cột theo ngày trong tuần
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card Donut Gauge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = EtymoTheme.cardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val trackCol = EtymoTheme.divider()
                                Canvas(modifier = Modifier.size(76.dp)) {
                                    drawArc(
                                        color = trackCol,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = Color(0xFF2563EB),
                                        startAngle = -90f,
                                        sweepAngle = 360f * animatedSweep,
                                        useCenter = false,
                                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                val pct = (animatedSweep * 100).toInt()
                                Text(
                                    text = "$pct%",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Đã thuộc / Tổng từ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$masteredCount / $totalSaved",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }

                    // Card Bar Chart 7 ngày trong tuần
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = EtymoTheme.cardBorder(),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tuần này",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB))
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "Đã học", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // 7 Cột biểu đồ T2 -> CN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val dailyValues = listOf(4, 2, 7, 5, 8, 3, 6)
                                val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

                                days.forEachIndexed { i, day ->
                                    val count = dailyValues[i]
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .width(12.dp)
                                                .height((count * 10).dp)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(if (i == 4) Color(0xFF2563EB) else Color(0xFF93C5FD))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = day,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Từ thường quên (Leech Words)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (leechWords.isNotEmpty()) "${leechWords.size} từ cần chú ý" else "0 từ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                // 4. Danh sách từ thường quên
                if (leechWords.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tuyệt vời! Không có từ nào bị quên.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Khi bạn đánh giá 'Không nhớ' trong các phiên ôn tập SRS, từ đó sẽ tự động xuất hiện ở đây để nhắc nhở rèn luyện thêm.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            leechWords.forEachIndexed { index, word ->
                                val rowInteraction = remember { MutableInteractionSource() }
                                val rowScale = rememberBounceScale(rowInteraction)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = rowScale
                                            scaleY = rowScale
                                        }
                                        .clickable(
                                            interactionSource = rowInteraction,
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onWordClick(word.headword)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (index < 2) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${word.rank}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index < 2) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = word.headword,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${word.posLabel})",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "${word.lastReviewedDaysAgo} trước",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFDC2626)
                                    )
                                }

                                if (index < leechWords.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StudyStatsTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Thống kê học tập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = { /* Options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimeframeChipsRow(
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("7 ngày", "30 ngày", "3 tháng", "Tùy chỉnh").forEach { label ->
            val isSelected = selected == label
            val interactionSource = remember { MutableInteractionSource() }
            val scale = rememberBounceScale(interactionSource)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else EtymoTheme.cardBorder(),
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelected(label) }
                    )
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 7.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: String,
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = EtymoTheme.cardBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
