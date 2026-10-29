package com.hanviet.etymoai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.VerificationStatus

/**
 * Huy hiệu trực quan nhận diện Nguồn gốc từ vựng (SINO_KOREAN, NATIVE_KOREAN, LOANWORD, MIXED).
 */
@Composable
fun OriginTypeBadge(
    originType: OriginType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, symbol) = when (originType) {
        OriginType.SINO_KOREAN -> Tuple4(
            Color(0xFFE8EAF6), // Indigo 50
            Color(0xFF1A237E), // Indigo 900
            Color(0xFF9FA8DA), // Indigo 200
            "漢"
        )
        OriginType.NATIVE_KOREAN -> Tuple4(
            Color(0xFFE0F2F1), // Teal 50
            Color(0xFF004D40), // Teal 900
            Color(0xFF80CBC4), // Teal 200
            "순"
        )
        OriginType.LOANWORD -> Tuple4(
            Color(0xFFFFF8E1), // Amber 50
            Color(0xFFE65100), // Amber 900
            Color(0xFFFFCC80), // Amber 200
            "외"
        )
        OriginType.MIXED -> Tuple4(
            Color(0xFFF3E5F5), // Purple 50
            Color(0xFF4A148C), // Purple 900
            Color(0xFFCE93D8), // Purple 200
            "혼"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = symbol,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = originType.titleVi,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Huy hiệu kiểm chứng học thuật (Academic Verification Status).
 * Tuân thủ nguyên tắc "Không bịa nguồn gốc".
 */
@Composable
fun VerificationStatusBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        VerificationStatus.VERIFIED_PRIMARY -> Triple(
            Color(0xFFE8F5E9), // Green 50
            Color(0xFF1B5E20), // Green 900
            Color(0xFFA5D6A7)  // Green 200
        )
        VerificationStatus.CURATED -> Triple(
            Color(0xFFE1F5FE), // Light Blue 50
            Color(0xFF01579B), // Light Blue 900
            Color(0xFF81D4FA)  // Light Blue 200
        )
        VerificationStatus.INFERRED -> Triple(
            Color(0xFFFFF3E0), // Orange 50
            Color(0xFFBF360C), // Orange 900
            Color(0xFFFFCC80)  // Orange 200
        )
        VerificationStatus.AI_SUGGESTED -> Triple(
            Color(0xFFFFFDE7), // Yellow 50
            Color(0xFFF57F17), // Yellow 900
            Color(0xFFFFF59D)  // Yellow 200
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (status) {
                VerificationStatus.VERIFIED_PRIMARY -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = textColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                VerificationStatus.CURATED -> {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Curated",
                        tint = textColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                VerificationStatus.INFERRED -> {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Inferred",
                        tint = textColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                VerificationStatus.AI_SUGGESTED -> {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Suggested",
                        tint = textColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.badgeLabel,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
