package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ObjectAnalysisResult
import com.example.model.StudyLanguage
import com.example.util.ShareExportOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareStudySheet(
    study: ObjectAnalysisResult,
    language: StudyLanguage,
    isPreparingExport: Boolean,
    onExportOption: (ShareExportOption) -> Unit,
    onCopyText: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!isPreparingExport) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Share & Export Study",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${study.objectTitle} · ${language.nativeName} · ${study.facts.size} facts & ${study.solutions.size} solutions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isPreparingExport,
                    modifier = Modifier.testTag("close_share_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close share sheet")
                }
            }

            if (isPreparingExport) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Rendering solution card & packaging images…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            ShareOptionCard(
                icon = Icons.Default.Share,
                title = "Share Full Guide & Solution Images",
                subtitle = "Exports structures, facts, solutions + Solution Card PNG & object photo",
                isPrimary = true,
                enabled = !isPreparingExport,
                testTag = "share_option_full",
                onClick = { onExportOption(ShareExportOption.FULL_PACKAGE) }
            )

            ShareOptionCard(
                icon = Icons.Default.Image,
                title = "Share Solution & Facts Card Image",
                subtitle = "Exports a visual PNG poster with facts, solutions & care tip for chats",
                isPrimary = false,
                enabled = !isPreparingExport,
                testTag = "share_option_solution_image",
                onClick = { onExportOption(ShareExportOption.SOLUTION_IMAGE) }
            )

            ShareOptionCard(
                icon = Icons.Default.Description,
                title = "Share Formatted Notes Only",
                subtitle = "Clean structured text for note-taking apps or low-bandwidth messaging",
                isPrimary = false,
                enabled = !isPreparingExport,
                testTag = "share_option_text_only",
                onClick = { onExportOption(ShareExportOption.TEXT_NOTES) }
            )

            ShareOptionCard(
                icon = Icons.Default.ContentCopy,
                title = "Copy Study Notes to Clipboard",
                subtitle = "Paste complete facts, structures & solutions directly into your notes",
                isPrimary = false,
                enabled = !isPreparingExport,
                testTag = "share_option_copy",
                onClick = onCopyText
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ShareOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isPrimary) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                modifier = Modifier.size(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPrimary) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
