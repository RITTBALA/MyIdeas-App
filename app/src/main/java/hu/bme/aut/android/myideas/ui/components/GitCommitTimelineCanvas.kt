package hu.bme.aut.android.myideas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.myideas.data.local.GitCommitEntity
import hu.bme.aut.android.myideas.ui.theme.CategoryDev
import hu.bme.aut.android.myideas.ui.theme.DarkBorder
import hu.bme.aut.android.myideas.ui.theme.DarkSurface
import hu.bme.aut.android.myideas.ui.theme.TextMuted
import hu.bme.aut.android.myideas.ui.theme.TextPrimary
import hu.bme.aut.android.myideas.ui.theme.TextSecondary

@Composable
fun GitCommitTimelineCanvas(
    commits: List<GitCommitEntity>,
    modifier: Modifier = Modifier
) {
    if (commits.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No GitHub commits recorded yet for this project.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        commits.forEachIndexed { index, commit ->
            val isFirst = index == 0
            val isLast = index == commits.size - 1

            CommitItemRow(
                commit = commit,
                isFirst = isFirst,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun CommitItemRow(
    commit: GitCommitEntity,
    isFirst: Boolean,
    isLast: Boolean
) {
    val shortSha = commit.sha.take(7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Git Commit Line & Dot Canvas
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val centerX = size.width / 2f
                val centerY = 24.dp.toPx()

                if (!isFirst) {
                    drawLine(
                        color = DarkBorder,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, centerY),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
                if (!isLast) {
                    drawLine(
                        color = DarkBorder,
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // Commit Badge Circle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.5.dp, CategoryDev, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Commit,
                    contentDescription = "Git Commit",
                    tint = CategoryDev,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Commit Content Card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            // Header Row: Short SHA + Author + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CategoryDev.copy(alpha = 0.15f))
                            .border(1.dp, CategoryDev.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "[$shortSha]",
                            style = MaterialTheme.typography.labelSmall,
                            color = CategoryDev,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = commit.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (commit.timestamp.isNotBlank()) {
                    Text(
                        text = commit.timestamp.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Commit Message
            Text(
                text = commit.messageTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!commit.messageBody.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = commit.messageBody,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
