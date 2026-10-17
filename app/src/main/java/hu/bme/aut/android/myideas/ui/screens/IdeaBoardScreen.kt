package hu.bme.aut.android.myideas.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.myideas.ui.components.CreateManualBottomSheet
import hu.bme.aut.android.myideas.ui.components.IdeaCard
import hu.bme.aut.android.myideas.ui.components.IdeaPreviewBottomSheet
import hu.bme.aut.android.myideas.ui.components.ProjectCard
import hu.bme.aut.android.myideas.ui.components.ProjectGraphDialog
import hu.bme.aut.android.myideas.ui.components.ProjectPreviewBottomSheet
import hu.bme.aut.android.myideas.ui.theme.AmberAccent
import hu.bme.aut.android.myideas.ui.theme.DarkBackground
import hu.bme.aut.android.myideas.ui.theme.DarkBorder
import hu.bme.aut.android.myideas.ui.theme.DarkSurface
import hu.bme.aut.android.myideas.ui.theme.DarkSurfaceVariant
import hu.bme.aut.android.myideas.ui.theme.TextMuted
import hu.bme.aut.android.myideas.ui.theme.TextPrimary
import hu.bme.aut.android.myideas.ui.theme.TextSecondary
import hu.bme.aut.android.myideas.ui.viewmodel.IdeaBoardViewModel

private val CATEGORIES = listOf("ALL", "DEV", "DIY", "HARDWARE", "WORKFLOW", "STUDY", "OTHER")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaBoardScreen(
    viewModel: IdeaBoardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var manualJsonInput by remember { mutableStateOf("") }

    // Display snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateManualBottomSheet() },
                containerColor = AmberAccent,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Manually"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Title
            Column {
                Text(
                    text = "IDEA STORE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Structured Thoughts & Git Commit Projects",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.scanClipboard(context, haptic) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paste",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.openCreateManualBottomSheet() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manual Add",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Switcher (0 = Ideas, 1 = Multi-node Projects)
            TabRow(
                selectedTabIndex = uiState.activeTab,
                containerColor = DarkSurface,
                contentColor = AmberAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab]),
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
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ideas (${uiState.ideas.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.activeTab == 0) AmberAccent else TextSecondary,
                                fontWeight = if (uiState.activeTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )

                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Projects (${uiState.projects.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.activeTab == 1) AmberAccent else TextSecondary,
                                fontWeight = if (uiState.activeTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search items...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AmberAccent
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CATEGORIES) { category ->
                    val isSelected = uiState.selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AmberAccent.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) AmberAccent else DarkBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.onCategorySelected(category) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "[$category]",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) AmberAccent else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smooth Tab Content Switcher
            Crossfade(
                targetState = uiState.activeTab,
                animationSpec = tween(durationMillis = 250),
                label = "TabContentCrossfade",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { activeTab ->
                if (activeTab == 0) {
                    // Single Ideas Tab
                    if (uiState.ideas.isEmpty()) {
                        EmptyStateCard(
                            title = "JSON copied to your clipboard will instantly turn into projects here.",
                            description = "Copy JSON v1 or v2 structure from your AI conversation, or create a new one manually.",
                            onScanClipboard = { viewModel.scanClipboard(context, haptic) },
                            onCreateManual = { viewModel.openCreateManualBottomSheet() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(
                                items = uiState.ideas,
                                key = { "idea_${it.id}" }
                            ) { idea ->
                                IdeaCard(
                                    idea = idea,
                                    onToggleFirstStep = { isDone ->
                                        viewModel.toggleFirstStepDone(idea.id, isDone, haptic)
                                    },
                                    onCopyJson = {
                                        viewModel.copyIdeaAsJson(context, idea, haptic)
                                    },
                                    onDeleteIdea = {
                                        viewModel.deleteIdea(idea)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Multi-node Projects Tab
                    if (uiState.projects.isEmpty()) {
                        EmptyStateCard(
                            title = "No multi-node projects found.",
                            description = "Copy the new v2 JSON structure, or create new Git commit projects manually.",
                            onScanClipboard = { viewModel.scanClipboard(context, haptic) },
                            onCreateManual = { viewModel.openCreateManualBottomSheet() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(
                                items = uiState.projects,
                                key = { "proj_${it.project.projectId}" }
                            ) { projectWithNodes ->
                                ProjectCard(
                                    projectWithNodes = projectWithNodes,
                                    isSyncing = uiState.isSyncingGithub,
                                    onOpenGraph = {
                                        viewModel.openProjectGraph(projectWithNodes)
                                    },
                                    onCopyJson = {
                                        viewModel.copyProjectAsJson(context, projectWithNodes, haptic)
                                    },
                                    onConnectRepo = { repoInput ->
                                        viewModel.connectRepoToProject(projectWithNodes.project.projectId, repoInput)
                                    },
                                    onPasteClipboardRepo = {
                                        viewModel.pasteAndConnectClipboardRepo(context, projectWithNodes.project.projectId)
                                    },
                                    onSyncCommits = {
                                        viewModel.syncGithubCommits(
                                            projectId = projectWithNodes.project.projectId,
                                            repoSlug = projectWithNodes.project.githubRepo ?: ""
                                        )
                                    },
                                    onUnlinkRepo = {
                                        viewModel.unlinkRepoFromProject(projectWithNodes.project.projectId)
                                    },
                                    onDeleteProject = {
                                        viewModel.deleteProject(projectWithNodes.project.projectId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Manual Creation
    if (uiState.isCreateManualBottomSheetVisible) {
        CreateManualBottomSheet(
            onSaveIdea = { title, summary, category, tags, firstStep, keyPoints ->
                viewModel.saveManualIdea(title, summary, category, tags, firstStep, keyPoints)
            },
            onSaveProject = { title, description, repo, category, nextMicroStep, initialNodeTitle, initialNodeType, initialNodeTag, initialNodeDetail ->
                viewModel.saveManualProject(title, description, repo, category, nextMicroStep, initialNodeTitle, initialNodeType, initialNodeTag, initialNodeDetail)
            },
            onDismiss = {
                viewModel.dismissCreateManualBottomSheet()
            }
        )
    }

    // Modal Bottom Sheet Preview for Schema v1 Ideas
    if (uiState.isIdeaSheetVisible && uiState.previewIdeaDto != null) {
        IdeaPreviewBottomSheet(
            dto = uiState.previewIdeaDto!!,
            onSave = { viewModel.savePreviewIdea() },
            onDismiss = { viewModel.dismissPreviewBottomSheet() }
        )
    }

    // Modal Bottom Sheet Preview for Schema v2 Multi-node Projects
    if (uiState.isProjectSheetVisible && uiState.previewProjectDto != null) {
        ProjectPreviewBottomSheet(
            dto = uiState.previewProjectDto!!,
            onSave = { viewModel.savePreviewProject() },
            onDismiss = { viewModel.dismissPreviewBottomSheet() }
        )
    }

    // Full Interactive Git Commit Graph Timeline View
    if (uiState.isGraphDialogVisible && uiState.selectedProjectForGraph != null) {
        ProjectGraphDialog(
            projectWithNodes = uiState.selectedProjectForGraph!!,
            onDismiss = { viewModel.dismissProjectGraph() }
        )
    }

    // Manual JSON Paste Dialog
    if (uiState.isManualPasteDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissManualPasteDialog() },
            title = {
                Text(
                    text = "Manual JSON Paste",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste the JSON v1 or v2 structure copied from your AI conversation:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = manualJsonInput,
                        onValueChange = { manualJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = {
                            Text(
                                text = "{\n  \"schema_version\": 2,\n  \"project_id\": \"...\",\n  \"nodes\": [...]\n}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AmberAccent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val textToParse = manualJsonInput
                        manualJsonInput = ""
                        viewModel.dismissManualPasteDialog()
                        viewModel.parseRawText(textToParse, haptic)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Process JSON", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        manualJsonInput = ""
                        viewModel.dismissManualPasteDialog()
                    }
                ) {
                    Text(text = "Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface,
            tonalElevation = 0.dp
        )
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    onScanClipboard: () -> Unit,
    onCreateManual: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DataObject,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons stacked vertically so they NEVER squish or overlap
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScanClipboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan Clipboard",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onCreateManual,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Manual Add", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
