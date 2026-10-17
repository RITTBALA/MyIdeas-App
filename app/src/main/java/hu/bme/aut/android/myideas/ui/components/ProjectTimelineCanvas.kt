package hu.bme.aut.android.myideas.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.myideas.data.local.NodeEntity
import hu.bme.aut.android.myideas.ui.theme.AmberAccent
import hu.bme.aut.android.myideas.ui.theme.CategoryDev
import hu.bme.aut.android.myideas.ui.theme.CategoryWorkflow
import hu.bme.aut.android.myideas.ui.theme.DarkBorder
import hu.bme.aut.android.myideas.ui.theme.DarkSurface
import hu.bme.aut.android.myideas.ui.theme.SageGreen
import hu.bme.aut.android.myideas.ui.theme.TextMuted
import hu.bme.aut.android.myideas.ui.theme.TextPrimary
import hu.bme.aut.android.myideas.ui.theme.TextSecondary

@Composable
fun ProjectTimelineCanvas(
    nodes: List<NodeEntity>,
    modifier: Modifier = Modifier
) {
    if (nodes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No nodes added to this project yet.",
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
            .animateContentSize()
    ) {
        nodes.forEachIndexed { index, node ->
            val isFirst = index == 0
            val isLast = index == nodes.size - 1

            NodeItemRow(
                node = node,
                isFirst = isFirst,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun NodeItemRow(
    node: NodeEntity,
    isFirst: Boolean,
    isLast: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    val nodeColor = remember(node.type) {
        when (node.type.uppercase()) {
            "DECISION" -> AmberAccent
            "MILESTONE" -> SageGreen
            "TASK_DONE" -> CategoryDev
            "OPEN_QUESTION" -> CategoryWorkflow
            else -> TextSecondary
        }
    }

    val nodeIcon = remember(node.type) {
        when (node.type.uppercase()) {
            "DECISION" -> Icons.Default.Lightbulb
            "MILESTONE" -> Icons.Default.MilitaryTech
            "TASK_DONE" -> Icons.Default.CheckCircle
            "OPEN_QUESTION" -> Icons.AutoMirrored.Filled.HelpOutline
            else -> Icons.Default.CheckCircle
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .animateContentSize(animationSpec = tween(220))
    ) {
        // Continuous Vertical Git Commit Line & Dot Canvas
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

                // Draw connecting vertical line extending top to bottom
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

            // Node Dot Badge
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.5.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = nodeIcon,
                    contentDescription = node.type,
                    tint = nodeColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Node Content Card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                .animateContentSize(animationSpec = tween(220))
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp)
        ) {
            // Header Row: Tag + Type Badge + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(nodeColor.copy(alpha = 0.15f))
                            .border(1.dp, nodeColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = node.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = nodeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "[${node.tag.uppercase()}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                if (node.timestamp.isNotBlank()) {
                    Text(
                        text = node.timestamp.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontSize = 14.sp
            )

            // Two-phase sequential animation: Container expands down FIRST, then text fades in
            if (!node.detail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = isExpanded || node.detail.length < 60,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 150, delayMillis = 160)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 120, delayMillis = 0)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(durationMillis = 200, delayMillis = 100)
                    ),
                    modifier = Modifier.clipToBounds()
                ) {
                    Text(
                        text = node.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (!isExpanded && node.detail.length >= 60) {
                    Text(
                        text = "Show details...",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
