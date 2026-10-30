package com.hanviet.etymoai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.VerificationStatus

/**
 * Trạng thái cơ sở dữ liệu từ điển cho OfflineStatusPill
 */
enum class DatabaseStatus {
    READY,      // [● Offline] nền xanh lá #DCFCE7, chữ #065F46
    UPDATING,   // [↻ Đang tải v1.3...] nền xanh dương #DBEAFE, chữ #1D4ED8
    ERROR       // [⚠️ Chưa cài DB] nền cam #FEF3C7, chữ #B45309
}

/**
 * Huy hiệu trực quan nhận diện Nguồn gốc từ vựng theo Semantic Color Tokens của Idea 2.2:
 * - Hán-Hàn (SINO_KOREAN): #EFF6FF / #2563EB, biểu tượng [漢]
 * - Thuần Hàn (NATIVE_KOREAN): #E0F2FE / #0284C7, biểu tượng 🌱
 * - Ngoại lai (LOANWORD): #FFF1F2 / #E11D48, biểu tượng 🔗
 * - Hỗn hợp (MIXED): #ECFDF5 / #059669, biểu tượng 🔀
 */
@Composable
fun OriginTypeBadge(
    originType: OriginType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, symbol) = when (originType) {
        OriginType.SINO_KOREAN -> Tuple4(
            Color(0xFFEFF6FF),
            Color(0xFF2563EB),
            Color(0xFFBFDBFE),
            "漢"
        )
        OriginType.NATIVE_KOREAN -> Tuple4(
            Color(0xFFE0F2FE),
            Color(0xFF0284C7),
            Color(0xFFBAE6FD),
            "🌱"
        )
        OriginType.LOANWORD -> Tuple4(
            Color(0xFFFFF1F2),
            Color(0xFFE11D48),
            Color(0xFFFECDD3),
            "🔗"
        )
        OriginType.MIXED -> Tuple4(
            Color(0xFFECFDF5),
            Color(0xFF059669),
            Color(0xFFA7F3D0),
            "🔀"
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = originType.titleVi,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Huy hiệu Trạng thái Xác minh Từ nguyên (VerificationStatus)
 */
@Composable
fun VerificationBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, dotColor) = when (status) {
        VerificationStatus.VERIFIED_PRIMARY -> Tuple4(
            Color(0xFFDCFCE7),
            Color(0xFF065F46),
            Color(0xFF86EFAC),
            Color(0xFF10B981)
        )
        VerificationStatus.CURATED -> Tuple4(
            Color(0xFFEFF6FF),
            Color(0xFF1D4ED8),
            Color(0xFF93C5FD),
            Color(0xFF3B82F6)
        )
        VerificationStatus.INFERRED -> Tuple4(
            Color(0xFFFFFBEB),
            Color(0xFF92400E),
            Color(0xFFFDE68A),
            Color(0xFFF59E0B)
        )
        VerificationStatus.AI_SUGGESTED -> Tuple4(
            Color(0xFFFAF5FF),
            Color(0xFF6B21A8),
            Color(0xFFE9D5FF),
            Color(0xFFA855F7)
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
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.badgeLabel,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Viên thuốc Trạng thái Gói Dữ liệu ([● Offline] Status Pill)
 * Hiển thị ở góc trên bên phải các màn hình chính theo chuẩn Idea 2.2
 */
@Composable
fun OfflineStatusPill(
    status: DatabaseStatus = DatabaseStatus.READY,
    versionLabel: String = "v1.2.0",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, dotColor, label) = when (status) {
        DatabaseStatus.READY -> Tuple5(
            Color(0xFFDCFCE7),
            Color(0xFF065F46),
            Color(0xFF86EFAC),
            Color(0xFF10B981),
            "● Offline"
        )
        DatabaseStatus.UPDATING -> Tuple5(
            Color(0xFFDBEAFE),
            Color(0xFF1D4ED8),
            Color(0xFF93C5FD),
            Color(0xFF2563EB),
            "↻ Đang tải $versionLabel"
        )
        DatabaseStatus.ERROR -> Tuple5(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            Color(0xFFFCD34D),
            Color(0xFFD97706),
            "⚠️ Chưa cài DB"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status == DatabaseStatus.READY) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Offline",
                    color = textColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (status == DatabaseStatus.UPDATING) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đang cập nhật...",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Chưa cài DB",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Hiệu ứng thu nhỏ thẻ khi chạm giữ (0.97x trong 80ms) theo chuẩn Motion Choreography FastOutSlowInEasing
 */
@Composable
fun rememberBounceScale(interactionSource: InteractionSource, pressedScale: Float = 0.97f): Float {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "CardPressBounce"
    )
    return scale
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
