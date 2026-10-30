package com.hanviet.etymoai.ui.hanja

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HanjaMasterInfo(
    val hanjaChar: String,
    val readingVi: String,
    val strokeCount: Int,
    val radicalChar: String,
    val radicalMeaning: String,
    val gradeLevel: String,
    val meaningSummary: String,
    val derivedWords: List<HanjaDerivedWord>
)

data class HanjaDerivedWord(
    val lexemeId: Long,
    val headwordKo: String,
    val hanjaCombo: String,
    val readingVi: String,
    val meaningVi: String
)

/**
 * Màn hình Khám phá & Đồ thị Họ Từ vựng Hán tự (HanjaExplorerScreen)
 * Triển khai theo thiết kế Section 6 của DESIGN.md
 */
@Composable
fun HanjaExplorerScreen(
    initialHanja: String = "學",
    onBackClick: () -> Unit = {},
    onWordClick: (Long, String) -> Unit = { _, _ -> },
    onSpeakWord: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val sampleHanjaList = remember {
        listOf(
            HanjaMasterInfo(
                hanjaChar = "學",
                readingVi = "HỌC",
                strokeCount = 16,
                radicalChar = "子",
                radicalMeaning = "Tử (đứa trẻ)",
                gradeLevel = "Cấp 8 (Sơ cấp)",
                meaningSummary = "Bắt chước, học tập, tiếp thu tri thức và rèn luyện kỹ năng",
                derivedWords = listOf(
                    HanjaDerivedWord(101L, "학교", "學校", "Học hiệu", "Trường học, cơ sở giáo dục đào tạo"),
                    HanjaDerivedWord(102L, "학생", "學生", "Học sinh", "Người theo học tại nhà trường"),
                    HanjaDerivedWord(103L, "학습", "學習", "Học tập", "Sự tiếp thu tri thức và rèn luyện"),
                    HanjaDerivedWord(104L, "학원", "學院", "Học viện", "Viện học thuật, trung tâm bồi dưỡng"),
                    HanjaDerivedWord(105L, "학문", "學問", "Học vấn", "Tri thức học thuật chuyên sâu"),
                    HanjaDerivedWord(106L, "장학금", "奬學金", "Tưởng học kim", "Học bổng khen thưởng học tập")
                )
            ),
            HanjaMasterInfo(
                hanjaChar = "生",
                readingVi = "SINH",
                strokeCount = 5,
                radicalChar = "生",
                radicalMeaning = "Sinh (mầm sống)",
                gradeLevel = "Cấp 8 (Sơ cấp)",
                meaningSummary = "Sinh nở, sinh ra, sự sống, người còn sống",
                derivedWords = listOf(
                    HanjaDerivedWord(201L, "선생님", "先生님", "Tiên sinh", "Người sinh trước, thầy giáo đáng kính"),
                    HanjaDerivedWord(202L, "학생", "學生", "Học sinh", "Người theo học tại nhà trường"),
                    HanjaDerivedWord(203L, "생활", "生活", "Sinh hoạt", "Đời sống, nếp sống thường nhật"),
                    HanjaDerivedWord(204L, "생명", "生命", "Sinh mệnh", "Tính mạng, mạng sống con người")
                )
            ),
            HanjaMasterInfo(
                hanjaChar = "圖",
                readingVi = "ĐỒ",
                strokeCount = 14,
                radicalChar = "囗",
                radicalMeaning = "Vi (khung bao)",
                gradeLevel = "Cấp 6 (Trung cấp)",
                meaningSummary = "Bản đồ, hình vẽ, dự tính, sách vở đồ họa",
                derivedWords = listOf(
                    HanjaDerivedWord(301L, "도서관", "圖書館", "Đồ thư quán", "Thư viện, nơi lưu trữ sách và tài liệu"),
                    HanjaDerivedWord(302L, "지도", "地圖", "Địa đồ", "Bản đồ chỉ dẫn địa lý"),
                    HanjaDerivedWord(303L, "의도", "意圖", "Ý đồ", "Ý định, mục đích dự tính")
                )
            ),
            HanjaMasterInfo(
                hanjaChar = "書",
                readingVi = "THƯ",
                strokeCount = 10,
                radicalChar = "曰",
                radicalMeaning = "Viết (nói rằng)",
                gradeLevel = "Cấp 7 (Sơ cấp)",
                meaningSummary = "Sách vở, văn tự, ghi chép thư tịch",
                derivedWords = listOf(
                    HanjaDerivedWord(401L, "도서관", "圖書館", "Đồ thư quán", "Thư viện lưu trữ sách"),
                    HanjaDerivedWord(402L, "교과서", "敎科書", "Giáo khoa thư", "Sách giáo khoa giảng dạy"),
                    HanjaDerivedWord(403L, "서점", "書店", "Thư điếm", "Hiệu sách, cửa hàng bán sách")
                )
            ),
            HanjaMasterInfo(
                hanjaChar = "館",
                readingVi = "QUÁN",
                strokeCount = 16,
                radicalChar = "食",
                radicalMeaning = "Thực (ăn uống)",
                gradeLevel = "Cấp 5 (Trung cấp)",
                meaningSummary = "Nhà trọ, tòa nhà công cộng, công sở chứa sách",
                derivedWords = listOf(
                    HanjaDerivedWord(501L, "도서관", "圖書館", "Đồ thư quán", "Thư viện sách"),
                    HanjaDerivedWord(502L, "박물관", "博物館", "Bác vật quán", "Bảo tàng lưu giữ hiện vật cổ"),
                    HanjaDerivedWord(503L, "대사관", "大使館", "Đại sứ quán", "Tòa nhà đại sứ ngoại giao")
                )
            )
        )
    }

    var selectedHanja by remember {
        mutableStateOf(
            sampleHanjaList.find { it.hanjaChar == initialHanja } ?: sampleHanjaList.first()
        )
    }

    Scaffold(
        topBar = {
            HanjaTopBar(onBackClick = onBackClick)
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
                // 1. Search Bar cho Chữ Hán
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        val matched = sampleHanjaList.find {
                            it.hanjaChar == query.trim() || it.readingVi.equals(query.trim(), ignoreCase = true)
                        }
                        if (matched != null) selectedHanja = matched
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Nhập chữ Hán hoặc âm Hán-Việt (Học, Sinh, Thư...)") },
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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )
            }

            // 2. Chip cuộn chọn chữ Hán nhanh
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleHanjaList) { item ->
                        val isSelected = item.hanjaChar == selectedHanja.hanjaChar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF1E3A8A) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.clickable { selectedHanja = item }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.hanjaChar,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.readingVi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFFBFDBFE) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Thẻ Thông Tin Hán Tự Hiện Hành
            item {
                HanjaMasterDetailCard(hanjaInfo = selectedHanja)
            }

            // 4. Tiêu đề danh sách từ họ chữ Hán
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HỌ TỪ VỰNG CHỨA CHỮ 【${selectedHanja.hanjaChar}】 (${selectedHanja.derivedWords.size} từ)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )

                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 5. Danh sách các từ dẫn xuất
            items(selectedHanja.derivedWords) { word ->
                HanjaDerivedWordCard(
                    word = word,
                    onClick = { onWordClick(word.lexemeId, word.headwordKo) },
                    onSpeak = { onSpeakWord(word.headwordKo) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HanjaTopBar(onBackClick: () -> Unit) {
    Surface(color = Color(0xFFF4F7FB), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Khám phá Chữ Hán (Hanja)",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun HanjaMasterDetailCard(hanjaInfo: HanjaMasterInfo) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Chữ Hán lớn trang nghiêm
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.5.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hanjaInfo.hanjaChar,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1E3A8A)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hanjaInfo.readingVi,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = hanjaInfo.gradeLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Bộ thủ: ${hanjaInfo.radicalChar} • ${hanjaInfo.radicalMeaning}",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Số nét: ${hanjaInfo.strokeCount} nét",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nghĩa chữ Hán: ${hanjaInfo.meaningSummary}",
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun HanjaDerivedWordCard(
    word: HanjaDerivedWord,
    onClick: () -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        text = word.headwordKo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${word.hanjaCombo})",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = word.readingVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = word.meaningVi,
                    fontSize = 12.5.sp,
                    color = Color(0xFF475569),
                    maxLines = 1
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
