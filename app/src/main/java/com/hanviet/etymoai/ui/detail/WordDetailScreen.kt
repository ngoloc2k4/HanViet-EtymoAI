package com.hanviet.etymoai.ui.detail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.EtymologyFamilyNode
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import com.hanviet.etymoai.domain.etymology.RelatedWordSummary
import com.hanviet.etymoai.domain.etymology.WordEtymology
import com.hanviet.etymoai.ui.components.OriginTypeBadge
import com.hanviet.etymoai.ui.components.VerificationStatusBadge
import kotlinx.coroutines.launch

/**
 * Màn hình Chi Tiết Từ Vựng & Đồ Thị Từ Nguyên (Word Detail & Etymology Screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    lexemeId: Long,
    repository: EtymologyRepository,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onWordClick: (Long) -> Unit = {}
) {
    var etymology by remember { mutableStateOf<WordEtymology?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedFamilyNode by remember { mutableStateOf<EtymologyFamilyNode?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(lexemeId) {
        isLoading = true
        etymology = repository.getEtymologyForWord(lexemeId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = etymology?.headword ?: "Chi Tiết Từ Vựng") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Lưu vào User Workspace */ }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Lưu từ"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                etymology == null -> {
                    Text(
                        text = "Không tìm thấy dữ liệu từ vựng.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    val wordData = etymology!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Header từ vựng + Phiên âm + Huy hiệu
                            WordHeaderSection(wordData)
                        }

                        item {
                            // Khối Chiết Tự Hán-Hàn-Việt (Etymology Breakdown)
                            EtymologyBreakdownView(
                                etymology = wordData,
                                onHanjaClick = { hanjaChar ->
                                    scope.launch {
                                        selectedFamilyNode = repository.getEtymologyFamily(hanjaChar)
                                        sheetState.show()
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            // BottomSheet hiển thị Cây Phả Hệ Từ Vựng cùng chữ Hán
            if (selectedFamilyNode != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedFamilyNode = null },
                    sheetState = sheetState
                ) {
                    EtymologyFamilyBottomSheetContent(
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
}

@Composable
private fun WordHeaderSection(wordData: WordEtymology) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = wordData.headword,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = { /* TODO: Phát âm TTS */ }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Nghe phát âm",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!wordData.phoneticVi.isNullOrBlank()) {
            Text(
                text = "[${wordData.phoneticVi}]",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hàng hiển thị các huy hiệu nguồn gốc và kiểm chứng
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OriginTypeBadge(originType = wordData.originType)
            VerificationStatusBadge(status = wordData.verificationStatus)
        }
    }
}

/**
 * Nội dung Bottom Sheet hiển thị danh sách các từ vựng họ hàng có chung chữ Hán
 */
@Composable
fun EtymologyFamilyBottomSheetContent(
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
        // Tiêu đề chữ Hán gốc
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = familyNode.hanjaChar,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Âm Hán-Việt: ${familyNode.readingVi.uppercase()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nghĩa: ${familyNode.meaningVi}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (familyNode.radical != null) {
                    Text(
                        text = "Bộ thủ: ${familyNode.radical} • ${familyNode.strokeCount ?: 0} nét",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Các từ vựng phái sinh có chung chữ '${familyNode.hanjaChar}':",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(260.dp)
        ) {
            items(familyNode.relatedWords) { word ->
                RelatedWordItem(word = word, onClick = { onWordClick(word.lexemeId) })
            }
        }
    }
}

@Composable
private fun RelatedWordItem(
    word: RelatedWordSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.headword,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!word.phoneticVi.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${word.phoneticVi})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = word.meaningVi,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
