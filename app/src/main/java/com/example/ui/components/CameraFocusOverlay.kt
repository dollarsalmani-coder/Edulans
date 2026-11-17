package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class FocusStatus {
    IDLE,
    FOCUSING,
    LOCKED,
    UNLOCKED
}

data class TapFocusState(
    val tapOffset: Offset? = null,
    val status: FocusStatus = FocusStatus.IDLE,
    val requestId: Long = 0L
)

@Composable
fun CameraFocusOverlay(
    focusState: TapFocusState,
    modifier: Modifier = Modifier
) {
    val tapOffset = focusState.tapOffset
    val visible = tapOffset != null && focusState.status != FocusStatus.IDLE
    val reticleSizeDp = 84.dp
    val halfSizePx = with(LocalDensity.current) { (reticleSizeDp / 2).toPx() }
    val scale = remember { Animatable(1.35f) }

    LaunchedEffect(focusState.requestId) {
        if (focusState.requestId > 0L) {
            scale.snapTo(1.35f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(120)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            if (tapOffset != null) {
                val ringColor = when (focusState.status) {
                    FocusStatus.FOCUSING -> Color.White
                    FocusStatus.LOCKED -> Color(0xFF4ADE80)
                    FocusStatus.UNLOCKED -> Color(0xFFFBBF24)
                    FocusStatus.IDLE -> Color.Transparent
                }
                Canvas(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (tapOffset.x - halfSizePx).roundToInt(),
                                y = (tapOffset.y - halfSizePx).roundToInt()
                            )
                        }
                        .size(reticleSizeDp)
                        .testTag("camera_focus_reticle")
                ) {
                    val baseRadius = (size.minDimension / 2f) * 0.72f * scale.value
                    val strokeWidth = 2.5.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = ringColor.copy(alpha = 0.22f),
                        radius = baseRadius,
                        center = center
                    )
                    drawCircle(
                        color = ringColor,
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    val tickLen = 7.dp.toPx()
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x, center.y - baseRadius - tickLen * 0.4f),
                        end = Offset(center.x, center.y - baseRadius + tickLen * 0.6f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x, center.y + baseRadius - tickLen * 0.6f),
                        end = Offset(center.x, center.y + baseRadius + tickLen * 0.4f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x - baseRadius - tickLen * 0.4f, center.y),
                        end = Offset(center.x - baseRadius + tickLen * 0.6f, center.y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x + baseRadius - tickLen * 0.6f, center.y),
                        end = Offset(center.x + baseRadius + tickLen * 0.4f, center.y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = ringColor,
                        radius = 3.dp.toPx(),
                        center = center
                    )
                }
            }
        }
    }
}
