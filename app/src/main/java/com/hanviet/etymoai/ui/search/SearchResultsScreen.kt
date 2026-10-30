package com.hanviet.etymoai.ui.search

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
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
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.search.SearchMatchTier
import com.hanviet.etymoai.domain.search.SearchPipeline
import com.hanviet.etymoai.domain.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Kiểu Badge hiển thị bên trái của mỗi thẻ kết quả tra cứu.
 */
sealed interface LeadingBadgeType {
    data class TextBadge(val text: String, val bg: Color, val fg: Color) : LeadingBadgeType
    data class IconBadge(val icon: androidx.compose.ui.graphics.vector.ImageVector, val bg: Color, val fg: Color) : LeadingBadgeType
}

/**
 * Mục kết quả tra cứu chuẩn hóa theo cấu trúc tầng ngôn ngữ Idea 2.2.
 */
data class SearchResultItem(
    val id: Long,
    val headword: String,
    val hanjaOriginal: String? = null,
    val originType: OriginType,
    val posTag: String,
    val pronunciationHangul: String,
    val pronunciationRoman: String,
    val primaryMeaningVi: String,
    val badgeType: LeadingBadgeType,
    val matchTierTitle: String
)

/**
 * Tùy chọn sắp xếp kết quả tra cứu.
 */
enum class SearchSortOption(val titleVi: String) {
    RELEVANCE("Liên quan nhất"),
    FREQUENCY("Phổ biến nhất"),
    ALPHABETICAL("Theo bảng chữ cái")
}

private fun SearchResult.toItem(): SearchResultItem {
    val origin = when (originType.uppercase()) {
        "SINO_KOREAN", "HANJA" -> OriginType.SINO_KOREAN
        "NATIVE", "NATIVE_KOREAN" -> OriginType.NATIVE_KOREAN
        "LOANWORD", "FOREIGN" -> OriginType.LOANWORD
        else -> OriginType.MIXED
    }
    val badge = when (origin) {
        OriginType.SINO_KOREAN -> LeadingBadgeType.TextBadge("漢", Color(0xFFEFF6FF), Color(0xFF2563EB))
        OriginType.NATIVE_KOREAN -> LeadingBadgeType.TextBadge("韓", Color(0xFFECFDF5), Color(0xFF059669))
        OriginType.LOANWORD -> LeadingBadgeType.TextBadge("🌐", Color(0xFFFFF1F2), Color(0xFFE11D48))
        OriginType.MIXED -> LeadingBadgeType.TextBadge("混", Color(0xFFFEF3C7), Color(0xFFD97706))
    }
    val tierLabel = when (matchTier) {
        SearchMatchTier.EXACT_HEADWORD -> "Khớp chính xác"
        SearchMatchTier.CHOSUNG_ALIAS -> "Khớp âm đầu (Chosung)"
        SearchMatchTier.PRECOMPUTED_FORM -> "Dạng biến thể tính sẵn"
        SearchMatchTier.MORPHOLOGY -> "Dạng gốc biến cách"
        SearchMatchTier.FTS_FULLTEXT -> "Khớp toàn văn (FTS5)"
    }
    return SearchResultItem(
        id = lexemeId,
        headword = headwordNfc,
        hanjaOriginal = null,
        originType = origin,
        posTag = if (!phoneticVi.isNullOrBlank()) "Hán-Việt: $phoneticVi" else "Từ vựng",
        pronunciationHangul = headwordNfc,
        pronunciationRoman = phoneticVi ?: "",
        primaryMeaningVi = primaryMeaningVi,
        badgeType = badge,
        matchTierTitle = tierLabel
    )
}

/**
 * Màn hình Kết Quả Tra Cứu (Search Results Screen) chuẩn Idea 2.2.
 * Hỗ trợ lọc theo 5 phân loại nguồn gốc (OriginType), phân cấp tầng kết quả rõ ràng,
 * kết nối trực tiếp SearchPipeline thực tế từ CSDL 50k NIKL.
 */
@Composable
fun SearchResultsScreen(
    initialQuery: String = "도서관",
    onWordClick: (Long, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {},
    searchPipeline: SearchPipeline? = null,
    onRecordHistory: ((String) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedOriginFilter by remember { mutableStateOf<OriginType?>(null) } // null = "Tất cả"
    var selectedSort by remember { mutableStateOf(SearchSortOption.RELEVANCE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var rawResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    fun executeSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            rawResults = emptyList()
            return
        }
        isSearching = true
        onRecordHistory?.invoke(q)
        scope.launch(Dispatchers.IO) {
            try {
                val results = searchPipeline?.search(q, limit = 50) ?: emptyList()
                val mapped = results.map { it.toItem() }
                withContext(Dispatchers.Main) {
                    rawResults = mapped
                    isSearching = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSearching = false
                }
            }
        }
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            executeSearch(initialQuery)
        }
    }

    // Lọc danh sách theo OriginType và từ khóa tìm kiếm
    val filteredResults = remember(rawResults, selectedOriginFilter, selectedSort) {
        val list = if (selectedOriginFilter == null) {
            rawResults
        } else {
            rawResults.filter { it.originType == selectedOriginFilter }
        }

        when (selectedSort) {
            SearchSortOption.RELEVANCE -> list
            SearchSortOption.FREQUENCY -> list.sortedBy { it.headword.length }
            SearchSortOption.ALPHABETICAL -> list.sortedBy { it.headword }
        }
    }

    Scaffold(
        bottomBar = {
            SearchResultsBottomBar(onTabClick = onNavigateTab)
        },
        containerColor = Color(0xFFF4F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Status Mock & Brand Header
            SearchResultsHeader(onMenuClick = { /* Show options */ })

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Search Input Box
            SearchInputBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = {
                    searchQuery = ""
                    rawResults = emptyList()
                },
                onFilterClick = {
                    // Chuyển đổi qua lại các bộ lọc nhanh
                    selectedOriginFilter = when (selectedOriginFilter) {
                        null -> OriginType.SINO_KOREAN
                        OriginType.SINO_KOREAN -> OriginType.NATIVE_KOREAN
                        OriginType.NATIVE_KOREAN -> OriginType.LOANWORD
                        OriginType.LOANWORD -> OriginType.MIXED
                        OriginType.MIXED -> null
                    }
                },
                onSubmit = {
                    focusManager.clearFocus()
                    executeSearch(searchQuery)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. OriginType Filter Chips (5 bộ lọc nguồn gốc theo chuẩn Idea 2.2)
            OriginFilterChipsRow(
                selectedOrigin = selectedOriginFilter,
                onSelectOrigin = { selectedOriginFilter = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Meta Row: Tìm thấy X kết quả & Menu sắp xếp
            ResultsMetaRow(
                resultCount = filteredResults.size,
                selectedSort = selectedSort,
                showSortMenu = showSortMenu,
                onToggleSortMenu = { showSortMenu = !showSortMenu },
                onSelectSort = {
                    selectedSort = it
                    showSortMenu = false
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Result Cards List
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔍",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Nhập từ khóa và bấm tìm kiếm" else "Không tìm thấy kết quả phù hợp cho \"$searchQuery\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
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
                    items(filteredResults, key = { it.id }) { item ->
                        SearchResultCard(
                            item = item,
                            onClick = { onWordClick(item.id, item.headword) },
                            onSpeak = { onSpeakWord(item.headword) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 1. Brand Header (Status & App Title Row)
 */
@Composable
private fun SearchResultsHeader(
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Brand bar: HanViet EtymoAI + Offline badge + Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔥",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HanViet EtymoAI",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Huy hiệu Offline (Chế độ cục bộ an toàn)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

/**
 * 2. Search Input Bar (Text Field + Clear ✕ + Divider + Advanced Filter ⊶)
 */
@Composable
private fun SearchInputBar(
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
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A1E3A8A)),
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
                contentDescription = "Tìm kiếm",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Nhập từ khóa tiếng Hàn hoặc Hán tự...",
                            fontSize = 15.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Đường kẻ dọc phân cách
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Nút bộ lọc nâng cao ⊶
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Bộ lọc nguồn gốc",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 3. OriginType Filter Chips Bar (5 bộ lọc: Tất cả, Hán-Hàn, Thuần Hàn, Ngoại lai, Hỗn hợp)
 */
@Composable
private fun OriginFilterChipsRow(
    selectedOrigin: OriginType?,
    onSelectOrigin: (OriginType?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chip "Tất cả"
        OriginFilterChip(
            label = "Tất cả",
            isSelected = selectedOrigin == null,
            onClick = { onSelectOrigin(null) }
        )

        // 4 OriginType Enums
        OriginType.entries.forEach { origin ->
            OriginFilterChip(
                label = origin.titleVi,
                isSelected = selectedOrigin == origin,
                onClick = { onSelectOrigin(origin) }
            )
        }
    }
}

@Composable
private fun OriginFilterChip(
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
 * 4. Meta Row: Tìm thấy X kết quả & Dropdown sắp xếp
 */
@Composable
private fun ResultsMetaRow(
    resultCount: Int,
    selectedSort: SearchSortOption,
    showSortMenu: Boolean,
    onToggleSortMenu: () -> Unit,
    onSelectSort: (SearchSortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tìm thấy $resultCount kết quả",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleSortMenu)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = selectedSort.titleVi,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Chọn sắp xếp",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = onToggleSortMenu
            ) {
                SearchSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (option == selectedSort) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = option.titleVi,
                                    fontWeight = if (option == selectedSort) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = { onSelectSort(option) }
                    )
                }
            }
        }
    }
}

/**
 * 5. Structured 3-Column Result Card (Cấu trúc 3 cột chuẩn Idea 2.2)
 * Cột 1: Rounded container 48x48dp chứa icon/chữ Hán
 * Cột 2: Từ khóa, huy hiệu gộp [Danh từ] [Hán-Hàn], phát âm, nghĩa tiếng Việt
 * Cột 3: Mũi tên điều hướng ›
 */
@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    onClick: () -> Unit,
    onSpeak: () -> Unit
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
            // Cột 1: 48x48dp rounded badge container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (val b = item.badgeType) {
                            is LeadingBadgeType.TextBadge -> b.bg
                            is LeadingBadgeType.IconBadge -> b.bg
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (val b = item.badgeType) {
                    is LeadingBadgeType.TextBadge -> {
                        Text(
                            text = b.text,
                            fontSize = if (b.text.length == 1 && b.text.first() in '\u4e00'..'\u9fff') 22.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = b.fg
                        )
                    }
                    is LeadingBadgeType.IconBadge -> {
                        Icon(
                            imageVector = b.icon,
                            contentDescription = null,
                            tint = b.fg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Cột 2: Nội dung từ vựng và phiên âm (Weight = 1f)
            Column(modifier = Modifier.weight(1f)) {
                // Dòng 1: Headword + Badges gộp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.headword,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Huy hiệu Nguồn gốc (Hán-Hàn, v.v.)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.originType) {
                            OriginType.SINO_KOREAN -> Color(0xFFDBEAFE)
                            OriginType.NATIVE_KOREAN -> Color(0xFFDCFCE7)
                            OriginType.LOANWORD -> Color(0xFFFEE2E2)
                            OriginType.MIXED -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Text(
                            text = item.originType.titleVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (item.originType) {
                                OriginType.SINO_KOREAN -> Color(0xFF1E40AF)
                                OriginType.NATIVE_KOREAN -> Color(0xFF15803D)
                                OriginType.LOANWORD -> Color(0xFFB91C1C)
                                OriginType.MIXED -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Huy hiệu Từ loại [Danh từ]
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = item.posTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dòng 2: Phiên âm + Nút loa phát âm
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[${item.pronunciationRoman}]",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Nghe phát âm",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Dòng 3: Nghĩa tiếng Việt
                Text(
                    text = item.primaryMeaningVi,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cột 3: Mũi tên điều hướng ›
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Chi tiết",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Bottom Navigation Bar (5 tabs) với tab Tra cứu được chọn
 */
@Composable
private fun SearchResultsBottomBar(
    onTabClick: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Tra cứu", Icons.Default.Search, 1),
            Triple("AI Chat", Icons.Default.ChatBubbleOutline, 2),
            Triple("Từ vựng", Icons.Default.BookmarkBorder, 3),
            Triple("Cá nhân", Icons.Default.Person, 4)
        )

        navItems.forEach { (label, icon, index) ->
            val isSelected = index == 1 // Đang ở tab Tra cứu

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
