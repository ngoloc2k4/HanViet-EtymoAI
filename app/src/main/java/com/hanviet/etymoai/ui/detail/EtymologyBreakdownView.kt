package com.hanviet.etymoai.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.WordEtymology

/**
 * Giao diện chiết tự từ nguyên âm tiết Hán-Hàn-Việt trực quan (Etymology Breakdown View).
 */
@Composable
fun EtymologyBreakdownView(
    etymology: WordEtymology,
    modifier: Modifier = Modifier,
    onHanjaClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (etymology.originType) {
                OriginType.SINO_KOREAN -> {
                    SinoKoreanBreakdownSection(
                        segments = etymology.segments,
                        onHanjaClick = onHanjaClick
                    )
                }
                OriginType.NATIVE_KOREAN -> {
                    NativeKoreanSection(headword = etymology.headword)
                }
                OriginType.LOANWORD -> {
                    LoanwordSection(
                        headword = etymology.headword,
                        adaptationNote = etymology.adaptationNote
                    )
                }
                OriginType.MIXED -> {
                    MixedBreakdownSection(
                        segments = etymology.segments,
                        onHanjaClick = onHanjaClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SinoKoreanBreakdownSection(
    segments: List<HanjaSegment>,
    onHanjaClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountTree,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Chiết Tự Hán-Hàn-Việt",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Text(
        text = "Nhấp vào từng chữ Hán để xem các từ vựng khác có chung gốc từ:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    if (segments.isEmpty()) {
        Text(
            text = "Chưa có thông tin phân đoạn chiết tự chi tiết cho từ này.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { segment ->
                HanjaSegmentCard(
                    segment = segment,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        segment.hanjaChar?.let { onHanjaClick(it) }
                    }
                )
            }
        }
    }
}

/**
 * Thẻ hiển thị một âm tiết chiết tự (Âm Hangul -> Chữ Hán -> Âm Hán-Việt -> Nghĩa).
 */
@Composable
fun HanjaSegmentCard(
    segment: HanjaSegment,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = segment.hanjaChar != null, onClick = onClick)
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Âm tiết tiếng Hàn
            Text(
                text = segment.syllableKo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Chữ Hán to bản rõ nét
            Text(
                text = segment.hanjaChar ?: "-",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Âm Hán-Việt viết hoa in đậm
            Text(
                text = segment.readingVi?.uppercase() ?: "-",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828) // Đỏ son truyền thống
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Nét nghĩa Hán-Việt ngắn gọn
            Text(
                text = segment.meaningVi ?: "",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NativeKoreanSection(headword: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF00796B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Từ vựng Thuần Hàn (순우리말)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "'$headword' là từ vựng thuần Hàn nguyên bản, không bắt nguồn từ chữ Hán. Từ loại này thường mang sắc thái biểu cảm, âm thanh và hiện tượng tự nhiên sâu sắc.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoanwordSection(headword: String, adaptationNote: String?) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Từ Ngoại Lai (외래어)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBF360C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = adaptationNote ?: "'$headword' là từ mượn từ ngôn ngữ phương Tây được phiên âm theo hệ thống ký âm Hangul.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MixedBreakdownSection(
    segments: List<HanjaSegment>,
    onHanjaClick: (String) -> Unit
) {
    Text(
        text = "Từ vựng Nguồn gốc Hỗn hợp (Hán-Hàn + Thuần Hàn)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    if (segments.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { segment ->
                HanjaSegmentCard(
                    segment = segment,
                    modifier = Modifier.weight(1f),
                    onClick = { segment.hanjaChar?.let { onHanjaClick(it) } }
                )
            }
        }
    }
}
