package com.hanviet.etymoai.ui.srs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.srs.ReviewRating
import com.hanviet.etymoai.domain.srs.SrsCard
import com.hanviet.etymoai.domain.srs.SrsFlashcardData
import com.hanviet.etymoai.ui.components.rememberBounceScale
import kotlinx.coroutines.launch

enum class SrsViewMode {
    TODAY, SCHEDULE, STATS
}

/**
 * Màn hình Ôn Tập & Flashcard SRS (SRS Review Session Screen - UX/UI v2.2)
 * Đặc tả Mục 11:
 * - Segmented Control (Hôm nay | Lịch trình | Thống kê) với hiệu ứng pill trượt 200ms
 * - Thẻ chỉ tiêu: 📅 Hôm nay: X thẻ | Đã học: Y | Còn lại: Z (AnimatedContent flip số)
 * - 3D Flashcard Flip (rotationY với cameraDistance, hai mặt độc lập trước/sau)
 * - Nút [💡 Xem gợi ý] từ nguyên Hán-Việt & ngữ cảnh
 * - Nút đánh giá: [✕ Không nhớ (10 phút)] và [✓ Đã nhớ (3 ngày)] với Haptic Feedback
 * - Mẹo ôn tập SRS và màn hình hoàn tất phiên học
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrsReviewScreen(
    cards: List<SrsFlashcardData>,
    onRatingSelected: (SrsCard, ReviewRating) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Review queue: copy cards to mutable queue to allow re-queueing lapsed cards
    val reviewQueue = remember(cards) { mutableStateListOf(*cards.toTypedArray()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var reviewedCount by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var selectedViewMode by remember { mutableStateOf(SrsViewMode.TODAY) }
    var showHintSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            SrsTopBar(onBackClick = onBackClick)
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (reviewQueue.isEmpty() || currentIndex >= reviewQueue.size) {
            SrsCompletedView(
                totalReviewed = reviewedCount.coerceAtLeast(cards.size),
                onRestart = {
                    reviewQueue.clear()
                    reviewQueue.addAll(cards)
                    currentIndex = 0
                    reviewedCount = 0
                    isFlipped = false
                },
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            val currentItem = reviewQueue[currentIndex]
            val totalInQueue = reviewQueue.size
            val remainingInQueue = (totalInQueue - currentIndex).coerceAtLeast(0)
            val progress = ((currentIndex + 1).toFloat() / totalInQueue.toFloat()).coerceIn(0f, 1f)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    // 1. Segmented Control 3 chế độ: [ Hôm nay ] | Lịch trình | Thống kê
                    SrsModeSegmentBar(
                        selectedMode = selectedViewMode,
                        onModeSelected = { mode ->
                            selectedViewMode = mode
                            if (mode == SrsViewMode.STATS) {
                                onNavigateToStats()
                            }
                        }
                    )
                }

                item {
                    // 2. Thẻ tóm tắt kế hoạch hôm nay với AnimatedContent
                    SrsDailyQuotaCard(
                        totalDue = cards.size,
                        reviewedCount = reviewedCount,
                        remainingCount = remainingInQueue
                    )
                }

                item {
                    // 3. Thẻ Flashcard 3D Lật trực quan (rotationY 0 -> 180)
                    Srs3DFlipCard(
                        item = currentItem,
                        isFlipped = isFlipped,
                        currentIndex = currentIndex + 1,
                        totalCards = totalInQueue,
                        progress = progress,
                        onCardClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isFlipped = !isFlipped
                        },
                        onSpeak = { onSpeakWord(currentItem.word.headwordSnapshot) }
                    )
                }

                item {
                    // 4. Hai nút đánh giá nhanh: [✕ Không nhớ] | [✓ Đã nhớ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nút Không nhớ
                        val failInteraction = remember { MutableInteractionSource() }
                        val failScale = rememberBounceScale(failInteraction)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = failScale
                                    scaleY = failScale
                                }
                                .clickable(
                                    interactionSource = failInteraction,
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRatingSelected(currentItem.card, ReviewRating.AGAIN)
                                    // Re-queue this card to end of session so user masters it
                                    reviewQueue.add(currentItem)
                                    isFlipped = false
                                    currentIndex++
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "10 phút",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB91C1C)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Không nhớ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }

                        // Nút Đã nhớ
                        val passInteraction = remember { MutableInteractionSource() }
                        val passScale = rememberBounceScale(passInteraction)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = passScale
                                    scaleY = passScale
                                }
                                .clickable(
                                    interactionSource = passInteraction,
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRatingSelected(currentItem.card, ReviewRating.GOOD)
                                    reviewedCount++
                                    isFlipped = false
                                    currentIndex++
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "3 ngày",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Đã nhớ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // 5. Nút [💡 Xem gợi ý] từ nguyên Hán-Việt
                    val hintInteraction = remember { MutableInteractionSource() }
                    val hintScale = rememberBounceScale(hintInteraction)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = hintScale
                                scaleY = hintScale
                            }
                            .clickable(
                                interactionSource = hintInteraction,
                                indication = null
                            ) {
                                scope.launch {
                                    showHintSheet = true
                                    sheetState.show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Xem gợi ý từ nguyên & ngữ cảnh",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                item {
                    // 6. Thẻ Mẹo ôn tập SRS
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🤖", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mẹo ôn tập SRS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Ôn tập đều đặn 5–10 phút mỗi ngày sẽ kích hoạt đường mòn trí nhớ dài hạn hiệu quả nhất!",
                                    fontSize = 12.sp,
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // BottomSheet gợi ý từ nguyên
        if (showHintSheet && reviewQueue.isNotEmpty() && currentIndex < reviewQueue.size) {
            val currentItem = reviewQueue[currentIndex]
            ModalBottomSheet(
                onDismissRequest = { showHintSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gợi ý liên tưởng từ nguyên",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentItem.hanjaSegments.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentItem.hanjaSegments.forEach { seg ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = seg.hanjaChar ?: "",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            text = "${seg.syllableKo} • ${seg.readingVi ?: ""}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                        Text(
                                            text = seg.meaningVi ?: "",
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Gợi ý: Từ này thuộc nhóm từ vựng cốt lõi. Hãy liên tưởng âm thanh Hangul để gợi nhớ nghĩa.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    if (!currentItem.exampleSentenceKo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Ngữ cảnh sử dụng:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentItem.exampleSentenceKo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun SrsTopBar(onBackClick: () -> Unit) {
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
                        tint = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ôn tập & Flashcard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun SrsModeSegmentBar(
    selectedMode: SrsViewMode,
    onModeSelected: (SrsViewMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SrsSegmentItem(
                label = "Hôm nay",
                isSelected = selectedMode == SrsViewMode.TODAY,
                onClick = { onModeSelected(SrsViewMode.TODAY) },
                modifier = Modifier.weight(1f)
            )

            SrsSegmentItem(
                label = "Lịch trình",
                isSelected = selectedMode == SrsViewMode.SCHEDULE,
                onClick = { onModeSelected(SrsViewMode.SCHEDULE) },
                modifier = Modifier.weight(1f)
            )

            SrsSegmentItem(
                label = "Thống kê",
                isSelected = selectedMode == SrsViewMode.STATS,
                onClick = { onModeSelected(SrsViewMode.STATS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SrsSegmentItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF2563EB) else Color.Transparent,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF64748B),
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Thẻ chỉ tiêu ôn tập: 📅 Hôm nay: X thẻ | Đã học: Y | Còn lại: Z (AnimatedContent flip số)
 */
@Composable
private fun SrsDailyQuotaCard(
    totalDue: Int,
    reviewedCount: Int,
    remainingCount: Int
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hôm nay",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$totalDue thẻ cần ôn",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Đã học",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                AnimatedContent(
                    targetState = reviewedCount,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "reviewedCountFlip"
                ) { count ->
                    Text(
                        text = "$count",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Còn lại",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                AnimatedContent(
                    targetState = remainingCount,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "remainingCountFlip"
                ) { count ->
                    Text(
                        text = "$count",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

/**
 * Thẻ Flashcard 3D với hiệu ứng lật rotationY và hiển thị 2 mặt rõ nét
 */
@Composable
private fun Srs3DFlipCard(
    item: SrsFlashcardData,
    isFlipped: Boolean,
    currentIndex: Int,
    totalCards: Int,
    progress: Float,
    onCardClick: () -> Unit,
    onSpeak: () -> Unit
) {
    val density = LocalDensity.current.density
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "cardFlipRotation"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A1E3A8A))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable(onClick = onCardClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Nền trang trí Canvas nhẹ
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val mountainPath = Path().apply {
                    moveTo(0f, h * 0.85f)
                    cubicTo(w * 0.25f, h * 0.65f, w * 0.5f, h * 0.75f, w, h * 0.8f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(mountainPath, Color(0xFFF8FAFC))
            }

            if (rotation <= 90f) {
                // Mặt trước (Front)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Huy hiệu nguồn gốc
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (item.word.originType) {
                            "SINO_KOREAN" -> Color(0xFFDBEAFE)
                            "LOANWORD" -> Color(0xFFFEE2E2)
                            "NATIVE_KOREAN", "NATIVE" -> Color(0xFFE0F2FE)
                            else -> Color(0xFFECFDF5)
                        }
                    ) {
                        val (label, textCol) = when (item.word.originType) {
                            "SINO_KOREAN" -> Pair("Hán-Hàn [漢]", Color(0xFF1E40AF))
                            "LOANWORD" -> Pair("Ngoại lai [🌐]", Color(0xFFBE123C))
                            "NATIVE_KOREAN", "NATIVE" -> Pair("Thuần Hàn [🌱]", Color(0xFF0369A1))
                            else -> Pair("Hỗn hợp [가]", Color(0xFF047857))
                        }
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textCol,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    // Trung tâm: Headword + Phát âm
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.word.headwordSnapshot,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Phát âm",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (!item.word.phoneticSnapshot.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "[${item.word.phoneticSnapshot}]",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chạm vào thẻ để lật xem nghĩa",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Tiến độ ở đáy
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$currentIndex / $totalCards",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            } else {
                // Mặt sau (Back) - Đảo ngược rotationY 180 để chữ không bị lộn ngược
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header mặt sau
                    Text(
                        text = "ĐÁP ÁN CHI TIẾT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        letterSpacing = 1.sp
                    )

                    // Nội dung nghĩa & từ nguyên
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = item.word.meaningSnapshot,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        if (!item.word.phoneticSnapshot.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hán-Việt: ${item.word.phoneticSnapshot}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }

                        if (!item.exampleSentenceKo.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ví dụ: ${item.exampleSentenceKo}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }

                    // Đáy thẻ mặt sau
                    Text(
                        text = "Chạm để lật lại mặt trước",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

/**
 * Giao diện khi hoàn thành toàn bộ lượt ôn tập
 */
@Composable
private fun SrsCompletedView(
    totalReviewed: Int,
    onRestart: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hoàn thành buổi ôn tập!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn đã ôn tập xuất sắc $totalReviewed thẻ vựng hôm nay. Trí nhớ dài hạn đã được củng cố!",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ôn tập lại")
            }
            Button(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Về Trang chủ")
            }
        }
    }
}
