package hu.bme.aut.android.myideas.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.myideas.ui.theme.AmberAccent
import hu.bme.aut.android.myideas.ui.theme.DarkBackground
import hu.bme.aut.android.myideas.ui.theme.DarkBorder
import hu.bme.aut.android.myideas.ui.theme.DarkSurface
import hu.bme.aut.android.myideas.ui.theme.DarkSurfaceVariant
import hu.bme.aut.android.myideas.ui.theme.TextMuted
import hu.bme.aut.android.myideas.ui.theme.TextPrimary
import hu.bme.aut.android.myideas.ui.theme.TextSecondary

private val CATEGORIES = listOf("DEV", "DIY", "HARDWARE", "WORKFLOW", "STUDY", "OTHER")
private val NODE_TYPES = listOf("DECISION", "TASK_DONE", "MILESTONE", "OPEN_QUESTION")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateManualBottomSheet(
    onSaveIdea: (title: String, summary: String, category: String, tags: String, firstStep: String?, keyPoints: String) -> Unit,
    onSaveProject: (title: String, description: String?, repo: String?, category: String, nextMicroStep: String?, initialNodeTitle: String, initialNodeType: String, initialNodeTag: String, initialNodeDetail: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Idea, 1 = Project

    // Idea Fields
    var ideaTitle by remember { mutableStateOf("") }
    var ideaSummary by remember { mutableStateOf("") }
    var ideaCategory by remember { mutableStateOf("DEV") }
    var ideaFirstStep by remember { mutableStateOf("") }
    var ideaKeyPoints by remember { mutableStateOf("") }
    var ideaTags by remember { mutableStateOf("") }

    // Project Fields
    var projTitle by remember { mutableStateOf("") }
    var projDescription by remember { mutableStateOf("") }
    var projRepo by remember { mutableStateOf("") }
    var projCategory by remember { mutableStateOf("HARDWARE") }
    var projNextStep by remember { mutableStateOf("") }
    var nodeTitle by remember { mutableStateOf("") }
    var nodeType by remember { mutableStateOf("DECISION") }
    var nodeTag by remember { mutableStateOf("INIT") }
    var nodeDetail by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = DarkBackground.copy(alpha = 0.85f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "MANUAL ADDITION",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                contentColor = AmberAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmberAccent,
                        height = 2.dp
                    )
                },
                divider = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "New Idea",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == 0) AmberAccent else TextSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "New Multi-node Project",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == 1) AmberAccent else TextSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // IDEA FORM
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TITLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ideaTitle,
                        onValueChange = { ideaTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. ESP32 BLE Hangboard", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CATEGORIES) { cat ->
                            val isSel = ideaCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) AmberAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSel) AmberAccent else DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable { ideaCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "[$cat]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) AmberAccent else TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "SUMMARY / DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ideaSummary,
                        onValueChange = { ideaSummary = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        placeholder = { Text("Project goals and key features...", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "FIRST STEP (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ideaFirstStep,
                        onValueChange = { ideaFirstStep = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Order components", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "KEY POINTS (COMMA OR NEWLINE SEPARATED)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ideaKeyPoints,
                        onValueChange = { ideaKeyPoints = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = { Text("Point 1, Point 2, Point 3", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "TAGS (COMMA SEPARATED)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ideaTags,
                        onValueChange = { ideaTags = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("esp32, ble, hardware", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveIdea(ideaTitle, ideaSummary, ideaCategory, ideaTags, ideaFirstStep, ideaKeyPoints)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Save Idea to Store", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            } else {
                // PROJECT FORM
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PROJECT TITLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = projTitle,
                        onValueChange = { projTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. ESP32 BLE Hangboard", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "PROJECT DESCRIPTION (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = projDescription,
                        onValueChange = { projDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = { Text("Detailed architecture and goals...", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "GITHUB REPO (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = projRepo,
                        onValueChange = { projRepo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("owner/repo or https://github.com/...", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CATEGORIES) { cat ->
                            val isSel = projCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) AmberAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSel) AmberAccent else DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable { projCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "[$cat]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) AmberAccent else TextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "CURRENT MICRO-STEP",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = projNextStep,
                        onValueChange = { projNextStep = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Sensor calibration", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "INITIAL GIT NODE (COMMIT)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "NODE TITLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nodeTitle,
                        onValueChange = { nodeTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. ESP32 selection", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "NODE TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(NODE_TYPES) { type ->
                            val isSel = nodeType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) AmberAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSel) AmberAccent else DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable { nodeType = type }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) AmberAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "NODE TAG",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nodeTag,
                        onValueChange = { nodeTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("HARDWARE, ARCH, BLE", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "DETAILS (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nodeDetail,
                        onValueChange = { nodeDetail = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        placeholder = { Text("Node details...", color = TextMuted) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveProject(projTitle, projDescription, projRepo, projCategory, projNextStep, nodeTitle, nodeType, nodeTag, nodeDetail)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Create Project", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant,
    focusedBorderColor = AmberAccent,
    unfocusedBorderColor = DarkBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = AmberAccent
)
