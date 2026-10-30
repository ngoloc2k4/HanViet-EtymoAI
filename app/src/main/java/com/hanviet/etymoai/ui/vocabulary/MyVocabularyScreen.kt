package com.hanviet.etymoai.ui.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.data.repository.UserWorkspaceRepository
import com.hanviet.etymoai.domain.etymology.OriginType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class SavedWordItem(
    val id: Long,
    val headword: String,
    val originType: OriginType,
    val posTag: String,
    val pronunciationHangul: String,
    val pronunciationRoman: String,
    val meaningVi: String,
    var isFavorite: Boolean = false
)

/**
 * Màn hình Từ Vựng Của Tôi (My Vocabulary Workspace Screen)
 * Bố cục tương đồng với Search Results nhưng bổ sung:
 * - Dòng tiêu đề phụ "Tổng số từ đã lưu: 128"
 * - Biểu tượng ngôi sao ★/☆ trên từng thẻ từ
 * - Nút tròn nổi (+) ở góc dưới phải để tự thêm từ cá nhân
 * - Chuẩn hóa nguồn gốc: 한국어 (韓國語) là SINO_KOREAN [漢], 알바 là LOANWORD [🌐].
 */
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
    var showAddWordDialog by remember { mutableStateOf(false) }
    var newWordHeadword by remember { mutableStateOf("") }
    var newWordMeaning by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Lấy dữ liệu từ vựng thực tế từ Workspace Database
    val savedWordsWithTags by (userWorkspaceRepository?.getAllSavedWordsFlow() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val savedWords = remember(savedWordsWithTags) {
        savedWordsWithTags.map { item ->
            val entity = item.word
            val origin = when (entity.originType.uppercase()) {
                "SINO_KOREAN", "HANJA" -> OriginType.SINO_KOREAN
                "NATIVE", "NATIVE_KOREAN" -> OriginType.NATIVE_KOREAN
                "LOANWORD", "FOREIGN" -> OriginType.LOANWORD
                else -> OriginType.MIXED
            }
            SavedWordItem(
                id = entity.id,
                headword = entity.headwordSnapshot,
                originType = origin,
                posTag = entity.phoneticSnapshot?.let { "Hán-Việt: $it" } ?: "Từ đã lưu",
                pronunciationHangul = entity.headwordSnapshot,
                pronunciationRoman = entity.phoneticSnapshot ?: "",
                meaningVi = entity.meaningSnapshot,
                isFavorite = entity.isFavorite
            )
        }
    }

    val filteredWords = remember(searchQuery, selectedOriginFilter, savedWords) {
        savedWords.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                item.headword.contains(searchQuery.trim(), ignoreCase = true) ||
                item.meaningVi.contains(searchQuery.trim(), ignoreCase = true)
            val matchFilter = selectedOriginFilter == null || item.originType == selectedOriginFilter
            matchQuery && matchFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            // Nút tròn nổi (+) ở góc dưới phải để tự thêm từ cá nhân
            FloatingActionButton(
                onClick = { showAddWordDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm từ mới"
                )
            }
        },
        containerColor = Color(0xFFF4F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Header & Dòng tiêu đề phụ
            MyVocabularyHeader(
                totalSavedCount = savedWords.size,
                onStartSrsReview = onStartSrsReview
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
                        Spacer(modifier = Modifier.height(80.dp)) // Chừa chỗ cho FAB
                    }
                }
            }
        }
    }

    // Dialog Thêm từ vựng cá nhân
    if (showAddWordDialog) {
        AlertDialog(
            onDismissRequest = { showAddWordDialog = false },
            title = {
                Text(
                    text = "Thêm từ vựng mới",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newWordHeadword,
                        onValueChange = { newWordHeadword = it },
                        label = { Text("Từ vựng tiếng Hàn (Headword)") },
                        placeholder = { Text("Ví dụ: 행복") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newWordMeaning,
                        onValueChange = { newWordMeaning = it },
                        label = { Text("Nghĩa tiếng Việt") },
                        placeholder = { Text("Ví dụ: hạnh phúc") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWordHeadword.isNotBlank()) {
                            val head = newWordHeadword.trim()
                            val mean = newWordMeaning.trim().ifBlank { "Từ vựng cá nhân" }
                            coroutineScope.launch {
                                userWorkspaceRepository?.saveWord(
                                    stableKey = "custom_${head}_${System.currentTimeMillis()}",
                                    headword = head,
                                    meaning = mean,
                                    originType = "SINO_KOREAN",
                                    isFavorite = true
                                )
                            }
                            newWordHeadword = ""
                            newWordMeaning = ""
                        }
                        showAddWordDialog = false
                    }
                ) {
                    Text("Lưu vào kho từ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWordDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun MyVocabularyHeader(
    totalSavedCount: Int,
    onStartSrsReview: () -> Unit = {}
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
                Text(
                    text = "Từ vựng của tôi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Dòng tiêu đề phụ
                Text(
                    text = "Tổng số từ đã lưu: $totalSavedCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2563EB)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDBEAFE),
                    modifier = Modifier.clickable { onStartSrsReview() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎴 Ôn tập SRS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = { /* Menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
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
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A1E3A8A)),
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
            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE2E8F0)))
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF2563EB) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.clickable(onClick = onClick)
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
 * Thẻ từ vựng đã lưu với biểu tượng ngôi sao ★/☆ và icon map chuẩn:
 * SINO_KOREAN: [漢]
 * LOANWORD: [🌐]
 * NATIVE_KOREAN: [🌱]
 * MIXED: [가]
 */
@Composable
private fun SavedWordCard(
    word: SavedWordItem,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
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
            // Cột 1: Icon container ánh xạ chính xác theo OriginType
            val (iconText, bgCol, fgCol) = when (word.originType) {
                OriginType.SINO_KOREAN -> Triple("漢", Color(0xFFEDE9FE), Color(0xFF6D28D9))
                OriginType.LOANWORD -> Triple("🌐", Color(0xFFFEE2E2), Color(0xFFDC2626))
                OriginType.NATIVE_KOREAN -> Triple("🌱", Color(0xFFDCFCE7), Color(0xFF15803D))
                OriginType.MIXED -> Triple("가", Color(0xFFFEF3C7), Color(0xFFD97706))
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
                            OriginType.NATIVE_KOREAN -> Color(0xFFDCFCE7)
                            OriginType.MIXED -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Text(
                            text = word.originType.titleVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (word.originType) {
                                OriginType.SINO_KOREAN -> Color(0xFF1E40AF)
                                OriginType.LOANWORD -> Color(0xFFB91C1C)
                                OriginType.NATIVE_KOREAN -> Color(0xFF15803D)
                                OriginType.MIXED -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cột 3: Biểu tượng ngôi sao ★/☆ để đánh dấu yêu thích
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (word.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Yêu thích",
                    tint = if (word.isFavorite) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun MyVocabularyBottomBar(
    onTabClick: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tra cứu", Icons.Default.Search, 1),
            Triple("AI Chat", Icons.Default.Psychology, 2),
            Triple("Từ vựng", Icons.Default.Bookmark, 3),
            Triple("Cá nhân", Icons.Default.Person, 4)
        )

        navItems.forEach { (label, icon, index) ->
            val isSelected = index == 3

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
