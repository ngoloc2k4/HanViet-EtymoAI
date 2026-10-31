package com.hanviet.etymoai.ui.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.ui.components.DatabaseStatus
import com.hanviet.etymoai.ui.components.OfflineStatusPill
import com.hanviet.etymoai.ui.components.rememberBounceScale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class SavedWordItem(
    val id: Long,
    val stableKey: String,
    val headword: String,
    val originType: OriginType,
    val posTag: String,
    val pronunciationHangul: String,
    val pronunciationRoman: String,
    val meaningVi: String,
    val personalNote: String = "",
    val isCustom: Boolean = false,
    var isFavorite: Boolean = false
)

/**
 * Màn hình Từ Vựng Của Tôi (My Vocabulary Workspace Screen - UX/UI v2.2)
 * Đặc tả Mục 10:
 * - Header có OfflineStatusPill và subtitle "Tổng số từ đã lưu: X"
 * - Biểu tượng ngôi sao ★/☆ trên từng thẻ từ với hiệu ứng bounce scale (0.5x -> 1.2x -> 1.0x) và Haptic Feedback
 * - Huy hiệu [Bản lưu cá nhân] phân biệt với từ cốt lõi NIKL
 * - ModalBottomSheet CustomWordBottomSheet với 4 trường nhập liệu + Switch tạo thẻ Flashcard SRS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVocabularyScreen(
    userWorkspaceRepository: UserWorkspaceRepository? = null,
    onWordClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onStartSrsReview: () -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOriginFilter by remember { mutableStateOf<OriginType?>(null) }
    var showAddWordSheet by remember { mutableStateOf(false) }
    var showOfflineInfoDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Lấy dữ liệu từ vựng thực tế từ Workspace Database
    val savedWordsWithTags by (userWorkspaceRepository?.getAllSavedWordsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val savedWords = remember(savedWordsWithTags) {
        savedWordsWithTags.map { item ->
            val entity = item.word
            val isCustomWord = entity.stableKey.startsWith("custom_")
            val origin = when (entity.originType.uppercase()) {
                "SINO_KOREAN", "HANJA" -> OriginType.SINO_KOREAN
                "NATIVE", "NATIVE_KOREAN" -> OriginType.NATIVE_KOREAN
                "LOANWORD", "FOREIGN" -> OriginType.LOANWORD
                else -> OriginType.MIXED
            }
            val firstNote = item.notes.firstOrNull()?.markdownContent ?: ""
            SavedWordItem(
                id = entity.id,
                stableKey = entity.stableKey,
                headword = entity.headwordSnapshot,
                originType = origin,
                posTag = if (isCustomWord) "Bản lưu cá nhân" else (entity.phoneticSnapshot?.let { "Hán-Việt: $it" } ?: "Từ đã lưu"),
                pronunciationHangul = entity.headwordSnapshot,
                pronunciationRoman = entity.phoneticSnapshot ?: "",
                meaningVi = entity.meaningSnapshot,
                personalNote = firstNote,
                isCustom = isCustomWord,
                isFavorite = entity.isFavorite
            )
        }
    }

    val filteredWords = remember(searchQuery, selectedOriginFilter, savedWords) {
        savedWords.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                item.headword.contains(searchQuery.trim(), ignoreCase = true) ||
                item.meaningVi.contains(searchQuery.trim(), ignoreCase = true) ||
                item.personalNote.contains(searchQuery.trim(), ignoreCase = true)
            val matchFilter = selectedOriginFilter == null || item.originType == selectedOriginFilter
            matchQuery && matchFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            val fabInteraction = remember { MutableInteractionSource() }
            val fabScale = rememberBounceScale(fabInteraction)
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAddWordSheet = true
                },
                interactionSource = fabInteraction,
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm từ mới"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Header & Dòng tiêu đề phụ
            MyVocabularyHeader(
                totalSavedCount = savedWords.size,
                onStartSrsReview = onStartSrsReview,
                onOfflinePillClick = { showOfflineInfoDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Search Bar
            MyVocabularySearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                onFilterClick = {
                    selectedOriginFilter = when (selectedOriginFilter) {
                        null -> OriginType.SINO_KOREAN
                        OriginType.SINO_KOREAN -> OriginType.NATIVE_KOREAN
                        OriginType.NATIVE_KOREAN -> OriginType.LOANWORD
                        OriginType.LOANWORD -> OriginType.MIXED
                        OriginType.MIXED -> null
                    }
                },
                onSubmit = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. 5 Chips Lọc OriginType
            MyVocabularyFilterChips(
                selectedOrigin = selectedOriginFilter,
                onSelectOrigin = { selectedOriginFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Danh sách từ vựng đã lưu
            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Không tìm thấy từ đã lưu cho \"$searchQuery\"" else "Chưa có từ vựng nào được lưu.\nHãy nhấn nút (+) để thêm từ hoặc lưu từ màn hình Tra cứu.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            lineHeight = 22.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredWords, key = { it.id }) { word ->
                        SavedWordCard(
                            word = word,
                            onClick = { onWordClick(word.headword) },
                            onSpeak = { onSpeakWord(word.headword) },
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    userWorkspaceRepository?.toggleFavorite(word.id)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp)) // Chừa chỗ cho FAB
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Thêm từ vựng cá nhân
    if (showAddWordSheet) {
        CustomWordBottomSheet(
            onDismiss = { showAddWordSheet = false },
            onSaveWord = { headword, meaning, note, example, enrollSrs ->
                coroutineScope.launch {
                    val combinedNote = buildString {
                        if (note.isNotBlank()) append(note.trim())
                        if (example.isNotBlank()) {
                            if (isNotEmpty()) append("\n\n")
                            append("Ví dụ: ").append(example.trim())
                        }
                    }
                    val wordId = userWorkspaceRepository?.saveWord(
                        stableKey = "custom_${headword.trim()}_${System.currentTimeMillis()}",
                        headword = headword.trim(),
                        meaning = meaning.trim().ifBlank { "Bản lưu cá nhân" },
                        originType = "SINO_KOREAN",
                        isFavorite = true,
                        note = combinedNote.ifBlank { null }
                    )
                    if (enrollSrs && wordId != null && wordId > 0) {
                        userWorkspaceRepository?.enrollInSrs(wordId)
                    }
                }
                showAddWordSheet = false
            }
        )
    }

    // Popup thông tin Offline Core Database
    if (showOfflineInfoDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineInfoDialog = false },
            title = {
                Text(
                    text = "Gói dữ liệu Offline NIKL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Phiên bản: Core NIKL v1.2.0 (52.000+ từ)\n• Bản quyền: CC BY-SA 2.0 KR\n• Trạng thái: Đã sẵn sàng tra cứu và lưu trữ offline 100%.",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showOfflineInfoDialog = false }) {
                    Text("Đóng", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
        )
    }
}

@Composable
private fun MyVocabularyHeader(
    totalSavedCount: Int,
    onStartSrsReview: () -> Unit = {},
    onOfflinePillClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Từ vựng của tôi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OfflineStatusPill(
                        status = DatabaseStatus.READY,
                        onClick = onOfflinePillClick
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                // Dòng tiêu đề phụ
                Text(
                    text = "Tổng số từ đã lưu: $totalSavedCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2563EB)
                )
            }

            val srsInteraction = remember { MutableInteractionSource() }
            val srsScale = rememberBounceScale(srsInteraction)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDBEAFE),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = srsScale
                        scaleY = srsScale
                    }
                    .clickable(
                        interactionSource = srsInteraction,
                        indication = null,
                        onClick = onStartSrsReview
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎴 Ôn tập SRS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D4ED8)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyVocabularySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onFilterClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A1E3A8A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Tìm kiếm trong kho từ đã lưu...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFFE2E8F0))
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onFilterClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Lọc",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MyVocabularyFilterChips(
    selectedOrigin: OriginType?,
    onSelectOrigin: (OriginType?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipItem(
            label = "Tất cả",
            isSelected = selectedOrigin == null,
            onClick = { onSelectOrigin(null) }
        )

        OriginType.entries.forEach { origin ->
            FilterChipItem(
                label = origin.titleVi,
                isSelected = selectedOrigin == origin,
                onClick = { onSelectOrigin(origin) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberBounceScale(interactionSource)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF2563EB) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

/**
 * Thẻ từ vựng đã lưu với biểu tượng ngôi sao ★/☆ và hiệu ứng bounce scale
 */
@Composable
private fun SavedWordCard(
    word: SavedWordItem,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardScale = rememberBounceScale(interactionSource)
    val haptic = LocalHapticFeedback.current

    // Star bounce animatable
    val starScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột 1: Icon container
            val (iconText, bgCol, fgCol) = when (word.originType) {
                OriginType.SINO_KOREAN -> Triple("漢", Color(0xFFEFF6FF), Color(0xFF2563EB))
                OriginType.LOANWORD -> Triple("🌐", Color(0xFFFFF1F2), Color(0xFFE11D48))
                OriginType.NATIVE_KOREAN -> Triple("🌱", Color(0xFFE0F2FE), Color(0xFF0284C7))
                OriginType.MIXED -> Triple("가", Color(0xFFECFDF5), Color(0xFF059669))
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgCol),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = fgCol
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Cột 2: Nội dung từ vựng
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.headword,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (word.originType) {
                            OriginType.SINO_KOREAN -> Color(0xFFDBEAFE)
                            OriginType.LOANWORD -> Color(0xFFFEE2E2)
                            OriginType.NATIVE_KOREAN -> Color(0xFFE0F2FE)
                            OriginType.MIXED -> Color(0xFFECFDF5)
                        }
                    ) {
                        Text(
                            text = word.originType.titleVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (word.originType) {
                                OriginType.SINO_KOREAN -> Color(0xFF1E40AF)
                                OriginType.LOANWORD -> Color(0xFFBE123C)
                                OriginType.NATIVE_KOREAN -> Color(0xFF0369A1)
                                OriginType.MIXED -> Color(0xFF047857)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (word.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "Bản lưu cá nhân",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${word.pronunciationRoman.ifBlank { word.pronunciationHangul }}]",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onSpeak, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Phát âm",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = word.meaningVi,
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (word.personalNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "💡 ${word.personalNote}",
                        fontSize = 12.sp,
                        color = Color(0xFF0D9488),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cột 3: Biểu tượng ngôi sao ★/☆ với scale bounce animation
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                        starScale.animateTo(
                            targetValue = 1f,
                            animationSpec = keyframes {
                                durationMillis = 200
                                0.5f at 0
                                1.25f at 100 using FastOutSlowInEasing
                                1.0f at 200 using FastOutSlowInEasing
                            }
                        )
                    }
                    onToggleFavorite()
                },
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = starScale.value
                        scaleY = starScale.value
                    }
            ) {
                Icon(
                    imageVector = if (word.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Yêu thích",
                    tint = if (word.isFavorite) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Bottom Sheet thêm từ vựng cá nhân với 4 trường nhập liệu và tuỳ chọn tạo SRS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomWordBottomSheet(
    onDismiss: () -> Unit,
    onSaveWord: (headword: String, meaning: String, note: String, example: String, enrollSrs: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var headword by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var enrollSrs by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thêm từ vựng cá nhân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Trường 1: Từ vựng tiếng Hàn
            OutlinedTextField(
                value = headword,
                onValueChange = { headword = it },
                label = { Text("Từ / Cụm từ tiếng Hàn (bắt buộc)") },
                placeholder = { Text("Ví dụ: 행복하다") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            // Trường 2: Nghĩa tiếng Việt
            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("Nghĩa tiếng Việt") },
                placeholder = { Text("Ví dụ: hạnh phúc, vui sướng") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            // Trường 3: Ghi chú cá nhân / Mẹo nhớ
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú cá nhân / Mẹo nhớ từ") },
                placeholder = { Text("Ví dụ: Hán-Hàn Hạnh Phúc, thường đi với gia đình") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            // Trường 4: Câu ví dụ tự soạn
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Câu ví dụ tự soạn") },
                placeholder = { Text("Ví dụ: 우리는 지금 매우 행복해요.") },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            // Switch SRS
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tạo thẻ Flashcard SRS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tự động đưa vào chu kỳ ôn tập ngắt quãng",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = enrollSrs,
                        onCheckedChange = { enrollSrs = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        )
                    )
                }
            }

            // Nút Lưu
            Button(
                onClick = {
                    if (headword.isNotBlank()) {
                        onSaveWord(headword, meaning, note, example, enrollSrs)
                    }
                },
                enabled = headword.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Lưu từ vựng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
