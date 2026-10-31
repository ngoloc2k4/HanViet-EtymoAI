package com.hanviet.etymoai.ui.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.EtymologyFamilyNode
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.RelatedWordSummary
import com.hanviet.etymoai.domain.etymology.VerificationStatus
import com.hanviet.etymoai.domain.etymology.WordEtymology
import com.hanviet.etymoai.domain.etymology.WordExample
import kotlinx.coroutines.launch

/**
 * Màn hình Chi Tiết Từ Vựng (Word Detail Screen - 도서관)
 * Thiết kế chuẩn Idea 2.2 với Hero Card, Thanh Tab nội bộ,
 * Visual Morpheme Equation (圖 도 • Đồ + 書 서 • Thư + 館 관 • Quán),
 * Bảng thông tin từ điển NIKL/KRDICT, và Cụm hành động AI / Offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    lexemeId: Long,
    headword: String? = null,
    repository: EtymologyRepository? = null,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onWordClick: (Long) -> Unit = {},
    onAskAiClick: (String) -> Unit = {},
    onSaveWord: (String) -> Unit = {},
    onSpeakWord: (String) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    var etymology by remember { mutableStateOf<WordEtymology?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBookmarked by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Tổng quan, 1: Từ nguyên, 2: Ví dụ, 3: Liên quan

    var selectedFamilyNode by remember { mutableStateOf<EtymologyFamilyNode?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(lexemeId, headword) {
        isLoading = true
        if (repository != null) {
            try {
                var data = if (lexemeId > 0) repository.getEtymologyForWord(lexemeId) else null
                if (data == null && !headword.isNullOrBlank()) {
                    data = repository.getEtymologyForWordByHeadword(headword.trim())
                }
                etymology = data
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Dữ liệu hiển thị nếu chưa có trong core db
        if (etymology == null) {
            val targetWord = if (!headword.isNullOrBlank()) headword.trim() else ""
            if (targetWord == "도서관") {
                etymology = WordEtymology(
                    lexemeId = if (lexemeId > 0) lexemeId else 101L,
                    headword = "도서관",
                    originType = OriginType.SINO_KOREAN,
                    verificationStatus = VerificationStatus.VERIFIED_PRIMARY,
                    phoneticVi = "Đồ Thư Quán",
                    segments = listOf(
                        HanjaSegment(
                            segmentIndex = 0,
                            syllableKo = "도",
                            hanjaChar = "圖",
                            readingVi = "Đồ",
                            meaningVi = "bản vẽ, hình ảnh, sách đồ án"
                        ),
                        HanjaSegment(
                            segmentIndex = 1,
                            syllableKo = "서",
                            hanjaChar = "書",
                            readingVi = "Thư",
                            meaningVi = "sách, văn bản, thư tịch"
                        ),
                        HanjaSegment(
                            segmentIndex = 2,
                            syllableKo = "관",
                            hanjaChar = "館",
                            readingVi = "Quán",
                            meaningVi = "tòa nhà, quán, cơ quan, viện"
                        )
                    ),
                    adaptationNote = "Đồ + Thư + Quán → Thư viện: nơi lưu trữ sách và tài liệu công cộng.",
                    partOfSpeech = "Danh từ",
                    meaningVi = "thư viện, nhà sách công cộng phục vụ đọc và mượn tư liệu",
                    definitionKo = "온갖 도서, 문서, 기록, 출판물 따위의 자료를 모아 두고 일반이 열람하게 하는 시설.",
                    examples = listOf(
                        WordExample("주말에는 국립중앙도서관에서 책을 읽거나 공부해요.", "Vào cuối tuần, tôi đọc sách hoặc học bài ở Thư viện Quốc gia."),
                        WordExample("도서관에서 책을 대출하고 반납했습니다.", "Tôi đã mượn và trả sách tại thư viện.")
                    ),
                    relatedWords = listOf(
                        RelatedWordSummary(101L, "도서", "Đồ thư", "sách vở, tư liệu"),
                        RelatedWordSummary(102L, "독서", "Độc thư", "việc đọc sách"),
                        RelatedWordSummary(103L, "서점", "Thư tiệm", "hiệu sách"),
                        RelatedWordSummary(104L, "박물관", "Bác vật quán", "viện bảo tàng")
                    )
                )
            } else {
                val displayWord = targetWord.ifBlank { if (lexemeId > 0) "Từ #$lexemeId" else "Từ vựng" }
                etymology = WordEtymology(
                    lexemeId = if (lexemeId > 0) lexemeId else System.currentTimeMillis(),
                    headword = displayWord,
                    originType = OriginType.NATIVE_KOREAN,
                    verificationStatus = VerificationStatus.INFERRED,
                    phoneticVi = null,
                    segments = emptyList(),
                    adaptationNote = "Từ vựng: $displayWord. Dữ liệu đang được chuẩn hóa trong CSDL offline.",
                    partOfSpeech = "Từ vựng",
                    meaningVi = "Từ vựng tiếng Hàn: $displayWord",
                    definitionKo = displayWord,
                    examples = emptyList(),
                    relatedWords = emptyList()
                )
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            WordDetailTopBar(
                onBackClick = onBackClick,
                isBookmarked = isBookmarked,
                onBookmarkToggle = {
                    isBookmarked = !isBookmarked
                    val wordToSave = etymology?.headword ?: headword?.trim() ?: ""
                    if (wordToSave.isNotBlank()) {
                        onSaveWord(wordToSave)
                    }
                },
                onMenuClick = { /* More actions */ }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            val word = etymology!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // 1. Thẻ Tóm Tắt Mục Từ (Hero Summary Card)
                    HeroSummaryCard(
                        word = word,
                        onSpeak = { onSpeakWord(word.headword) }
                    )
                }

                item {
                    // 2. Thanh Tab Điều Hướng Nội Bộ (In-page Anchor / Tab Bar)
                    WordDetailInternalTabs(
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }

                // 3. Nội dung hiển thị chuyển đổi theo Tab được kích hoạt
                when (selectedTabIndex) {
                    0 -> {
                        // TAB 0: TỔNG QUAN (Thông tin từ điển + Chiết tự tóm tắt + Ví dụ mẫu)
                        item {
                            DictionaryInfoCard(
                                word = word,
                                onSpeak = { onSpeakWord(word.headword) }
                            )
                        }
                        item {
                            VisualMorphemeEquationCard(
                                word = word,
                                onHanjaClick = { hanja ->
                                    scope.launch {
                                        val node = repository?.getEtymologyFamily(hanja) ?: EtymologyFamilyNode(
                                            hanjaChar = hanja,
                                            readingVi = when (hanja) {
                                                "圖" -> "Đồ"
                                                "書" -> "Thư"
                                                "館" -> "Quán"
                                                else -> "Hán"
                                            },
                                            meaningVi = when (hanja) {
                                                "圖" -> "bản vẽ, hình ảnh, mưu toan"
                                                "書" -> "sách vở, viết văn"
                                                "館" -> "tòa nhà, dinh quán, viện"
                                                else -> "Ý nghĩa Hán tự"
                                            },
                                            radical = when (hanja) {
                                                "圖" -> "囗"
                                                "書" -> "曰"
                                                "館" -> "食"
                                                else -> null
                                            },
                                            strokeCount = when (hanja) {
                                                "圖" -> 14
                                                "書" -> 10
                                                "館" -> 16
                                                else -> null
                                            },
                                            relatedWords = listOf(
                                                RelatedWordSummary(101L, "도서", "Đồ thư", "sách, tư liệu"),
                                                RelatedWordSummary(102L, "독서", "Độc thư", "việc đọc sách"),
                                                RelatedWordSummary(103L, "서점", "Thư tiệm", "hiệu sách")
                                            )
                                        )
                                        selectedFamilyNode = node
                                        sheetState.show()
                                    }
                                }
                            )
                        }
                        if (word.examples.isNotEmpty()) {
                            item {
                                ExampleSentenceCard(
                                    examples = word.examples.take(2),
                                    onSpeakSentence = onSpeakWord
                                )
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: TỪ NGUYÊN (Visual Morpheme Equation chi tiết + Phả hệ chiết tự + Thích ứng ngữ âm)
                        item {
                            VisualMorphemeEquationCard(
                                word = word,
                                onHanjaClick = { hanja ->
                                    scope.launch {
                                        val node = repository?.getEtymologyFamily(hanja) ?: EtymologyFamilyNode(
                                            hanjaChar = hanja,
                                            readingVi = "Hán",
                                            meaningVi = "Ý nghĩa Hán tự"
                                        )
                                        selectedFamilyNode = node
                                        sheetState.show()
                                    }
                                }
                            )
                        }
                        item {
                            EtymologyAdaptationCard(word = word)
                        }
                    }

                    2 -> {
                        // TAB 2: VÍ DỤ (Toàn bộ các câu ví dụ từ điển NIKL kèm âm thanh)
                        item {
                            ExampleSentenceCard(
                                examples = if (word.examples.isNotEmpty()) word.examples else listOf(
                                    WordExample("${word.headword} 문장을 공부합니다.", "Tôi học câu ví dụ với từ ${word.headword}."),
                                    WordExample("선생님께서 ${word.headword}에 대해 설명하셨습니다.", "Thầy giáo đã giải thích về ${word.headword}.")
                                ),
                                onSpeakSentence = onSpeakWord
                            )
                        }
                    }

                    3 -> {
                        // TAB 3: LIÊN QUAN (Từ vựng đồng gốc Hán tự + Cụm từ thường gặp)
                        item {
                            OriginAndRelatedCard(
                                word = word,
                                onWordClick = onWordClick,
                                onChipClick = onAskAiClick
                            )
                        }
                    }
                }

                // 7. Cụm Nút Hành Động Cuối Trang (Bottom Action Area)
                item {
                    BottomActionArea(
                        headword = word.headword,
                        isBookmarked = isBookmarked,
                        onAskAi = { onAskAiClick(word.headword) },
                        onBookmark = {
                            isBookmarked = !isBookmarked
                            onSaveWord(word.headword)
                        },
                        onFlashcard = {
                            onSaveWord(word.headword)
                        },
                        onShare = { /* Share dialog */ }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // BottomSheet Phả hệ Chữ Hán
        if (selectedFamilyNode != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedFamilyNode = null },
                sheetState = sheetState
            ) {
                EtymologyFamilySheetContent(
                    familyNode = selectedFamilyNode!!,
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedFamilyNode = null
                        }
                    },
                    onWordClick = { relatedId ->
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedFamilyNode = null
                            onWordClick(relatedId)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Top App Bar
 */
@Composable
private fun WordDetailTopBar(
    onBackClick: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onMenuClick: () -> Unit
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
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Chi tiết từ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Lưu từ",
                            tint = if (isBookmarked) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = onMenuClick) {
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
 * 1. Hero Summary Card (Thẻ Tóm Tắt Mục Từ)
 * Biểu tượng chữ Hán [書], từ khóa 도서관, nhãn Hán-Hàn, phát âm kèm loa,
 * và thanh 3 thông số xác thực (📖 Danh từ | 📊 Trung cấp | 🛡️ KRDICT)
 */
@Composable
private fun HeroSummaryCard(
    word: WordEtymology,
    onSpeak: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A0F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Khối đại diện chữ Hán 54x54dp (ví dụ 書 - Thư)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val repChar = word.segments.firstOrNull { it.hanjaChar?.isNotBlank() == true }?.hanjaChar
                        ?: word.headword.take(1)
                    Text(
                        text = repChar,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF6D28D9)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Headword + Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = word.headword,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = word.originType.titleVi,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Phiên âm chuẩn Hangul + Latinh + Loa phát âm
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pronunciationDisplay = if (!word.phoneticVi.isNullOrBlank()) {
                            "[${word.headword} • ${word.phoneticVi}]"
                        } else {
                            "[${word.headword}]"
                        }
                        Text(
                            text = pronunciationDisplay,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Nghe phát âm",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val meaningDisplay = word.meaningVi ?: word.definitionKo ?: "Từ vựng tiếng Hàn"
                    Text(
                        text = meaningDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Thanh 3 thông số xác thực: 📖 Từ loại | 📊 Cấp độ | 🛡️ Nguồn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thông số 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📖", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = word.partOfSpeech ?: "Từ vựng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(Color(0xFFE2E8F0))
                )

                // Thông số 2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trung cấp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(Color(0xFFE2E8F0))
                )

                // Thông số 3: Huy hiệu bảo chứng KRDICT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "KRDICT",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KRDICT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

/**
 * 2. Thanh Tab Điều Hướng Nội Bộ (In-page Anchor / Tab Bar)
 * 4 Tabs: Tổng quan, Từ nguyên, Ví dụ, Liên quan
 */
@Composable
private fun WordDetailInternalTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Tổng quan", "🔗 Từ nguyên", "📖 Ví dụ", "🔗 Liên quan")

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = Color(0xFF2563EB),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = Color(0xFF2563EB),
                height = 3.dp
            )
        },
        divider = {
            HorizontalDivider(color = Color(0xFFE2E8F0))
        }
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
                    )
                }
            )
        }
    }
}

/**
 * 3. Khối ♊ Phân tích từ nguyên (Visual Morpheme Equation)
 * Hiển thị bộ ba [圖] 도 • Đồ | [書] 서 • Thư | [館] 관 • Quán
 * kèm hộp ý nghĩa tổng hợp vàng kem
 */
@Composable
private fun VisualMorphemeEquationCard(
    word: WordEtymology,
    onHanjaClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "♊", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Phân tích từ nguyên",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (word.segments.isNotEmpty()) {
                // Các thẻ chữ Hán nối với nhau bằng dấu +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val segments = word.segments

                    segments.forEachIndexed { index, seg ->
                        MorphemeSegmentBox(
                            segment = seg,
                            onClick = { onHanjaClick(seg.hanjaChar ?: "") },
                            modifier = Modifier.weight(1f)
                        )

                        if (index < segments.size - 1) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ℹ️", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Từ vựng thuộc nhóm ${word.originType.titleVi}, không cấu tạo từ các thành tố Hán tự.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hộp Ý nghĩa tổng hợp màu vàng kem (💡 Ý nghĩa tổng hợp)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFFBEB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ý nghĩa tổng hợp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val defaultNote = if (!word.meaningVi.isNullOrBlank()) {
                        "${word.headword}: ${word.meaningVi}"
                    } else {
                        "Từ vựng tiếng Hàn: ${word.headword}"
                    }

                    Text(
                        text = word.adaptationNote ?: defaultNote,
                        fontSize = 13.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Một ô thành tố chữ Hán trong công thức chiết tự: [Hanja] Hangul • Hán-Việt
 */
@Composable
private fun MorphemeSegmentBox(
    segment: HanjaSegment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chữ Hán
            Text(
                text = segment.hanjaChar ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bộ đôi Âm Hangul • Âm Hán Việt: [도 • Đồ]
            Text(
                text = "${segment.syllableKo} • ${segment.readingVi ?: ""}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Giải nghĩa vắn tắt
            Text(
                text = segment.meaningVi ?: "",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 4. Khối 🕘 Thông tin từ điển (Dictionary Info Table - Căn thẳng dấu hai chấm)
 */
@Composable
private fun DictionaryInfoCard(
    word: WordEtymology,
    onSpeak: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🕘", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Thông tin từ điển",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val hanjaFull = word.segments.mapNotNull { it.hanjaChar }.joinToString("").ifBlank { "—" }
            val hanVietFull = word.segments.mapNotNull { it.readingVi }.joinToString(" - ").ifBlank { word.phoneticVi ?: "—" }

            DictionaryInfoRow(label = "Từ gốc Hán tự", value = hanjaFull, isHanja = hanjaFull != "—")
            DictionaryInfoRow(label = "Âm Hán-Việt", value = hanVietFull, isHighlight = true)
            DictionaryInfoRow(label = "Từ loại", value = word.partOfSpeech ?: "Danh từ")
            DictionaryInfoRow(label = "Nguồn gốc", value = word.originType.titleVi)
            DictionaryInfoPronounceRow(
                label = "Phát âm",
                hangul = "[${word.headword}]",
                roman = word.phoneticVi ?: word.headword,
                onSpeak = onSpeak
            )
            DictionaryInfoRow(
                label = "Nghĩa tiếng Việt",
                value = word.meaningVi ?: "Chưa có định nghĩa tiếng Việt"
            )
            if (!word.definitionKo.isNullOrBlank()) {
                DictionaryInfoRow(label = "Định nghĩa Hàn", value = word.definitionKo)
            }
        }
    }
}

@Composable
private fun DictionaryInfoRow(
    label: String,
    value: String,
    isHanja: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.width(115.dp)
        )

        Text(
            text = ":",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Text(
            text = value,
            fontSize = if (isHanja) 15.sp else 13.sp,
            fontWeight = if (isHighlight || isHanja) FontWeight.Bold else FontWeight.Medium,
            fontFamily = if (isHanja) FontFamily.Serif else FontFamily.Default,
            color = if (isHighlight) Color(0xFF2563EB) else Color(0xFF1E293B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DictionaryInfoPronounceRow(
    label: String,
    hangul: String,
    roman: String,
    onSpeak: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.width(115.dp)
        )

        Text(
            text = ":",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$hangul / $roman",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onSpeak,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 5. Khối 🔗 Nguồn gốc & Liên quan (Origin & Co-etymology)
 */
@Composable
private fun OriginAndRelatedCard(
    word: WordEtymology,
    onWordClick: (Long) -> Unit,
    onChipClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔗", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Từ vựng liên quan & Cùng gốc",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (word.relatedWords.isNotEmpty()) {
                Text(
                    text = "Từ có chung Hán tự gốc (${word.relatedWords.size}):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    word.relatedWords.forEach { rel ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onWordClick(rel.lexemeId) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = rel.headword,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        if (!rel.phoneticVi.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${rel.phoneticVi}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rel.meaningVi,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = "Cụm từ tra cứu nhanh:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Chips cụm từ tương tác
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("${word.headword}에 가다", "${word.headword} 공부", "${word.headword} 활용")
                chips.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { onChipClick(chipText) }
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 6. Khối 📖 Ví dụ câu thực tế
 */
@Composable
private fun ExampleSentenceCard(
    examples: List<WordExample>,
    onSpeakSentence: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📖", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ví dụ câu ngữ cảnh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "${examples.size} câu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            examples.forEachIndexed { idx, ex ->
                if (idx > 0) {
                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ex.korean,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = ex.vietnamese,
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                    }

                    IconButton(
                        onClick = { onSpeakSentence(ex.korean) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Phát âm câu",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Khối Ghi chú thích ứng chuyển ngữ (Tab Từ nguyên)
 */
@Composable
private fun EtymologyAdaptationCard(word: WordEtymology) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x140F172A)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📜", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quy tắc ngữ âm & Thích ứng chuyển ngữ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF3C7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🏛️", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chứng nhận: ${word.verificationStatus.badgeLabel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val note = word.adaptationNote ?: if (word.originType == OriginType.SINO_KOREAN) {
                "Từ vựng gốc Hán du nhập vào bán đảo Triều Tiên, trải qua quá trình thích ứng ngữ âm Hán-Hàn và tương ứng chặt chẽ với hệ thống âm Hán-Việt."
            } else {
                "Từ thuần Hàn bản địa, mang đặc trưng ngữ âm và ngữ nghĩa độc lập của Hàn ngữ nguyên thủy."
            }

            Text(
                text = note,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 7. Cụm Nút Hành Động Cuối Trang (Bottom Action Area)
 * Tầng 1: Nút lớn màu xanh tím 🧠 Hỏi AI về từ này ›
 * Tầng 2: 3 nút offline [🔖 Lưu từ vựng] [🗂️ Tạo flashcard] [🔗 Chia sẻ]
 */
@Composable
private fun BottomActionArea(
    headword: String,
    isBookmarked: Boolean,
    onAskAi: () -> Unit,
    onBookmark: () -> Unit,
    onFlashcard: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tầng 1: Nút Gradient nổi bật "🧠 Hỏi AI về từ này ›"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onAskAi)
                .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0x334F46E5)),
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF2563EB))
                        )
                    )
                    .padding(vertical = 14.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🧠", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hỏi AI về từ này",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Tầng 2: 3 nút tính năng lưu trữ Offline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nút Lưu từ vựng
            SecondaryActionButton(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (isBookmarked) "Đã lưu" else "Lưu từ",
                tint = if (isBookmarked) Color(0xFF2563EB) else Color(0xFF475569),
                onClick = onBookmark,
                modifier = Modifier.weight(1f)
            )

            // Nút Tạo Flashcard
            SecondaryActionButton(
                icon = Icons.Default.Style,
                label = "Tạo flashcard",
                tint = Color(0xFF475569),
                onClick = onFlashcard,
                modifier = Modifier.weight(1f)
            )

            // Nút Chia sẻ
            SecondaryActionButton(
                icon = Icons.Default.Share,
                label = "Chia sẻ",
                tint = Color(0xFF475569),
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SecondaryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

/**
 * Bottom Navigation Bar (5 tabs)
 */
@Composable
private fun WordDetailBottomBar(
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
            val isSelected = index == 1 // Từ chi tiết tra cứu

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

/**
 * BottomSheet nội dung phả hệ chữ Hán
 */
@Composable
private fun EtymologyFamilySheetContent(
    familyNode: EtymologyFamilyNode,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onWordClick: (Long) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = familyNode.hanjaChar,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Âm Hán-Việt: ${familyNode.readingVi.uppercase()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Nghĩa: ${familyNode.meaningVi}",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
                if (familyNode.radical != null) {
                    Text(
                        text = "Bộ thủ: ${familyNode.radical} • ${familyNode.strokeCount ?: 0} nét",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Các từ vựng phái sinh có chung chữ '${familyNode.hanjaChar}':",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            items(familyNode.relatedWords) { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onWordClick(word.lexemeId) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = word.headword,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                            if (!word.phoneticVi.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${word.phoneticVi})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = word.meaningVi,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}
