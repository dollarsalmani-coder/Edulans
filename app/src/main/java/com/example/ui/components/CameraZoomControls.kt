package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

fun clampZoomRatio(
    targetRatio: Float,
    minRatio: Float,
    maxRatio: Float
): Float {
    val safeMin = minRatio.coerceAtLeast(0.5f)
    val safeMax = maxRatio.coerceAtLeast(safeMin)
    return targetRatio.coerceIn(safeMin, safeMax)
}

fun formatZoomLabel(zoomRatio: Float): String {
    return String.format(Locale.US, "%.1fx", zoomRatio)
}

@Composable
fun CameraZoomControls(
    currentZoomRatio: Float,
    minZoomRatio: Float,
    maxZoomRatio: Float,
    onSelectZoomRatio: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = buildList {
        if (minZoomRatio < 0.85f) {
            add(minZoomRatio)
        }
        add(1.0f.coerceIn(minZoomRatio, maxZoomRatio))
        if (maxZoomRatio >= 1.9f) {
            add(2.0f)
        }
        if (maxZoomRatio >= 3.0f) {
            add(3.0f)
        } else if (maxZoomRatio >= 1.3f && maxZoomRatio < 1.9f) {
            add(maxZoomRatio)
        }
    }.distinct()

    Surface(
        color = Color.Black.copy(alpha = 0.52f),
        shape = RoundedCornerShape(50),
        modifier = modifier.testTag("camera_zoom_controls")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Zoom level",
                tint = Color.White.copy(alpha = 0.88f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp)
            )
            presets.forEach { preset ->
                val isSelected = abs(currentZoomRatio - preset) < 0.12f
                val displayText = if (isSelected && abs(currentZoomRatio - preset) >= 0.05f) {
                    formatZoomLabel(currentZoomRatio)
                } else {
                    formatZoomLabel(preset)
                }
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Color.White else Color.Transparent
                        )
                        .clickable(enabled = maxZoomRatio > minZoomRatio) {
                            onSelectZoomRatio(clampZoomRatio(preset, minZoomRatio, maxZoomRatio))
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("camera_zoom_preset_${formatZoomLabel(preset)}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
            if (presets.none { abs(currentZoomRatio - it) < 0.12f }) {
                Text(
                    text = formatZoomLabel(currentZoomRatio),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("camera_zoom_live_readout")
                )
            }
        }
    }
}
