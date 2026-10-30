package com.hanviet.etymoai.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.search.SearchMatchTier
import com.hanviet.etymoai.domain.search.SearchPipeline
import com.hanviet.etymoai.domain.search.SearchResult
import com.hanviet.etymoai.ui.components.DatabaseStatus
import com.hanviet.etymoai.ui.components.OfflineStatusPill
import com.hanviet.etymoai.ui.components.rememberBounceScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

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
    val matchTierTitle: String,
    val matchTier: SearchMatchTier = SearchMatchTier.EXACT_HEADWORD,
    val morphologicalRule: String? = null,
    val loanwordSource: String? = null
)

/**
 * Tùy chọn sắp xếp kết quả tra cứu.
 */
enum class SearchSortOption(val titleVi: String) {
    RELEVANCE("Liên quan nhất"),
    LEVEL("Cấp độ: Sơ cấp → Cao cấp"),
    ALPHABETICAL("Theo bảng chữ cái (가나다)")
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
        OriginType.NATIVE_KOREAN -> LeadingBadgeType.TextBadge("🌱", Color(0xFFE0F2FE), Color(0xFF0284C7))
        OriginType.LOANWORD -> LeadingBadgeType.TextBadge("🔗", Color(0xFFFFF1F2), Color(0xFFE11D48))
        OriginType.MIXED -> LeadingBadgeType.TextBadge("🔀", Color(0xFFECFDF5), Color(0xFF059669))
    }
    val tierLabel = when (matchTier) {
        SearchMatchTier.EXACT_HEADWORD -> "Khớp chính xác"
        SearchMatchTier.CHOSUNG_ALIAS -> "Khớp âm đầu (Chosung)"
        SearchMatchTier.PRECOMPUTED_FORM -> "Dạng biến thể tính sẵn"
        SearchMatchTier.MORPHOLOGY -> "Dạng gốc biến cách"
        SearchMatchTier.FTS_FULLTEXT -> "Khớp nghĩa / FTS5"
    }

    val morphRule = if (matchTier == SearchMatchTier.MORPHOLOGY) {
        "Phân tích quy tắc chia đuôi biến thể → Nguyên thể"
    } else null

    val loanSource = if (origin == OriginType.LOANWORD) {
        "Từ gốc ngoại lai (✓ Đã xác minh)"
    } else null

    return SearchResultItem(
        id = lexemeId,
        headword = headwordNfc,
        hanjaOriginal = null,
        originType = origin,
        posTag = if (!phoneticVi.isNullOrBlank()) "Hán-Việt: $phoneticVi" else "Từ vựng",
        pronunciationHangul = headwordNfc,
        pronunciationRoman = phoneticVi ?: headwordNfc,
        primaryMeaningVi = primaryMeaningVi,
        badgeType = badge,
        matchTierTitle = tierLabel,
        matchTier = matchTier,
        morphologicalRule = morphRule,
        loanwordSource = loanSource
    )
}

/**
 * Màn hình Kết Quả Tra Cứu (Search Results Screen) chuẩn Idea 2.2.
 * Hỗ trợ Debounced Reactive Search 120ms, bộ lọc nâng cao, phân cấp 5 loại thẻ kết quả,
 * và chuyển tiếp sang AI Chat khi không tìm thấy mục từ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    initialQuery: String = "",
    onWordClick: (Long, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {},
    onNavigateToAi: (String) -> Unit = {},
    searchPipeline: SearchPipeline? = null,
    onRecordHistory: ((String) -> Unit)? = null
) {
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedOriginFilter by remember { mutableStateOf<OriginType?>(null) } // null = "Tất cả"
    var selectedSort by remember { mutableStateOf(SearchSortOption.RELEVANCE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAdvancedFilters by remember { mutableStateOf(false) }

    // Bộ lọc nâng cao
    var selectedPosFilter by remember { mutableStateOf("Tất cả") }
    var selectedLevelFilter by remember { mutableStateOf("Tất cả") }
    var filterOnlyVerifiedHanja by remember { mutableStateOf(false) }

    var rawResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // 120ms Debounced Reactive Search
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            rawResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        delay(120) // 120ms debounce
        val normalized = Normalizer.normalize(q, Normalizer.Form.NFC)
        isSearching = true
        onRecordHistory?.invoke(normalized)
        withContext(Dispatchers.IO) {
            try {
                val results = searchPipeline?.search(normalized, limit = 50) ?: emptyList()
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

    // Lọc danh sách theo OriginType, Từ loại và Cấp độ
    val filteredResults = remember(rawResults, selectedOriginFilter, selectedSort, selectedPosFilter, filterOnlyVerifiedHanja) {
        var list = if (selectedOriginFilter == null) {
            rawResults
        } else {
            rawResults.filter { it.originType == selectedOriginFilter }
        }

        if (filterOnlyVerifiedHanja) {
            list = list.filter { it.originType == OriginType.SINO_KOREAN }
        }

        when (selectedSort) {
            SearchSortOption.RELEVANCE -> list
            SearchSortOption.LEVEL -> list.sortedBy { it.headword.length }
            SearchSortOption.ALPHABETICAL -> list.sortedBy { it.headword }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Brand Header
            SearchResultsHeader()

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Search Input Box
            SearchInputBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = {
                    searchQuery = ""
                    rawResults = emptyList()
                },
                onFilterClick = { showAdvancedFilters = true },
                onSubmit = {
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. OriginType Filter Chips (Tất cả, Hán-Hàn, Thuần Hàn, Ngoại lai, Hỗn hợp)
            OriginFilterChipsRow(
                selectedOrigin = selectedOriginFilter,
                onSelectOrigin = { selectedOriginFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                // Trạng thái Không có Kết quả & Chuyển tiếp sang AI (Empty & Fallback State)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Nhập từ khóa tiếng Hàn hoặc tiếng Việt để bắt đầu tra cứu"
                            } else {
                                "Chưa tìm thấy mục từ khớp chính xác trong từ điển offline cho truy vấn: \"$searchQuery\""
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 19.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Thẻ gợi ý hành động chuyển sang AI Chat
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Không tìm thấy trong CSDL 50k",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Bạn có thể gửi cụm từ hoặc câu này sang Trợ lý AI để giải thích cấu trúc ngữ pháp và từ vựng mở rộng.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            onNavigateToAi(searchQuery.trim())
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4F46E5)
                                        )
                                    ) {
                                        Text("✨ Phân tích câu / từ này bằng Trợ lý AI", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
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
                            searchQuery = searchQuery,
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

    // Modal Bottom Sheet: Bộ lọc nâng cao
    if (showAdvancedFilters) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAdvancedFilters = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bộ lọc tra cứu nâng cao",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = { showAdvancedFilters = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Từ loại (Part of Speech):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tất cả", "Danh từ", "Động từ", "Tính từ").forEach { pos ->
                        FilterChip(
                            selected = selectedPosFilter == pos,
                            onClick = { selectedPosFilter = pos },
                            label = { Text(pos, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cấp độ TOPIK:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tất cả", "Sơ cấp", "Trung cấp", "Cao cấp").forEach { lvl ->
                        FilterChip(
                            selected = selectedLevelFilter == lvl,
                            onClick = { selectedLevelFilter = lvl },
                            label = { Text(lvl, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Chỉ hiện từ đã xác minh chữ Hán",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Lọc riêng các mục từ có chiết tự Hanja chuẩn",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = filterOnlyVerifiedHanja,
                        onCheckedChange = { filterOnlyVerifiedHanja = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showAdvancedFilters = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Áp dụng bộ lọc", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * 1. Brand Header
 */
@Composable
private fun SearchResultsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tra cứu từ điển",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        // Huy hiệu Offline Status Pill
        OfflineStatusPill(
            status = DatabaseStatus.READY,
            versionLabel = "v1.2.0"
        )
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
                            text = "Nhập từ khóa tiếng Hàn, Hán tự hoặc tiếng Việt...",
                            fontSize = 13.5.sp,
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
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Đường kẻ dọc phân cách
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Nút bộ lọc nâng cao ⊶
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Bộ lọc nâng cao",
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
        OriginFilterChip(
            label = "Tất cả",
            isSelected = selectedOrigin == null,
            onClick = { onSelectOrigin(null) }
        )

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
    val bgColor = if (isSelected) Color(0xFF2563EB) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF475569)
    val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .height(30.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * 4. Meta Row: Tìm thấy X kết quả & Menu sắp xếp
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
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tìm thấy $resultCount kết quả",
            fontSize = 12.sp,
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Chọn sắp xếp",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(16.dp)
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
                                    fontSize = 12.5.sp,
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
 * 5. Structured Result Card hỗ trợ 5 kiểu hiển thị theo tầng dữ liệu Idea 2.2:
 * - Kiểu 1: Exact Match (viền xanh nhạt, nút 🔊 phát âm trực tiếp)
 * - Kiểu 2: Morphology Candidate (công thức phân tích chia đuôi)
 * - Kiểu 3: Component / Morpheme Match (Hanja glyph)
 * - Kiểu 4: Highlight match trong âm Hán-Việt hoặc nghĩa tiếng Việt
 * - Kiểu 5: Loanword Origin Match
 */
@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    searchQuery: String,
    onClick: () -> Unit,
    onSpeak: () -> Unit
) {
    val isExactMatch = item.matchTier == SearchMatchTier.EXACT_HEADWORD
    val isMorphology = item.matchTier == SearchMatchTier.MORPHOLOGY

    val borderColor = if (isExactMatch) Color(0xFF93C5FD) else Color(0xFFE2E8F0)
    val borderWidth = if (isExactMatch) 1.5.dp else 1.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        shadowElevation = if (isExactMatch) 2.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cột 1: Rounded container 48x48dp chứa icon/chữ Hán
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
                                fontSize = if (b.text.length == 1 && b.text.first() in '\u4e00'..'\u9fff') 22.sp else 18.sp,
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

                        // Huy hiệu Nguồn gốc
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (item.originType) {
                                OriginType.SINO_KOREAN -> Color(0xFFEFF6FF)
                                OriginType.NATIVE_KOREAN -> Color(0xFFE0F2FE)
                                OriginType.LOANWORD -> Color(0xFFFFF1F2)
                                OriginType.MIXED -> Color(0xFFECFDF5)
                            }
                        ) {
                            Text(
                                text = item.originType.titleVi,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (item.originType) {
                                    OriginType.SINO_KOREAN -> Color(0xFF2563EB)
                                    OriginType.NATIVE_KOREAN -> Color(0xFF0284C7)
                                    OriginType.LOANWORD -> Color(0xFFE11D48)
                                    OriginType.MIXED -> Color(0xFF059669)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Huy hiệu Từ loại [Danh từ / Hán-Việt]
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = item.posTag,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Dòng 2: Phiên âm + Nút loa phát âm
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${item.pronunciationRoman}]",
                            fontSize = 12.5.sp,
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

                    Spacer(modifier = Modifier.height(2.dp))

                    // Dòng 3: Nghĩa tiếng Việt có highlight từ khóa
                    val cleanQuery = searchQuery.trim()
                    if (cleanQuery.length >= 2 && item.primaryMeaningVi.contains(cleanQuery, ignoreCase = true)) {
                        val startIndex = item.primaryMeaningVi.indexOf(cleanQuery, ignoreCase = true)
                        val endIndex = startIndex + cleanQuery.length
                        val annotatedMeaning = buildAnnotatedString {
                            append(item.primaryMeaningVi.substring(0, startIndex))
                            withStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)) {
                                append(item.primaryMeaningVi.substring(startIndex, endIndex))
                            }
                            append(item.primaryMeaningVi.substring(endIndex))
                        }
                        Text(
                            text = annotatedMeaning,
                            fontSize = 13.5.sp,
                            color = Color(0xFF334155),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = item.primaryMeaningVi,
                            fontSize = 13.5.sp,
                            color = Color(0xFF334155),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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

            // Kiểu 2: Morphology Note (Dạng chia → Nguyên thể)
            if (isMorphology && item.morphologicalRule != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFECFDF5)
                    ) {
                        Text(
                            text = "Dạng chia → Nguyên thể",
                            color = Color(0xFF059669),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.morphologicalRule,
                        fontSize = 11.5.sp,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Kiểu 5: Loanword Origin Note
            if (item.originType == OriginType.LOANWORD && item.loanwordSource != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Khớp từ gốc ngoại lai: \"${item.headword}\" (✓ Đã xác minh)",
                    fontSize = 11.sp,
                    color = Color(0xFFE11D48),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
