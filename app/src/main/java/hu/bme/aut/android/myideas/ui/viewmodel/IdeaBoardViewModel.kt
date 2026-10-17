package hu.bme.aut.android.myideas.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.myideas.data.local.IdeaEntity
import hu.bme.aut.android.myideas.data.local.ProjectWithNodes
import hu.bme.aut.android.myideas.data.model.CurrentHeadDto
import hu.bme.aut.android.myideas.data.model.IdeaDto
import hu.bme.aut.android.myideas.data.model.NodeDto
import hu.bme.aut.android.myideas.data.model.ProjectDto
import hu.bme.aut.android.myideas.data.network.GithubLogService
import hu.bme.aut.android.myideas.data.parser.ParseResult
import hu.bme.aut.android.myideas.data.repository.IdeaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

data class IdeaBoardUiState(
    val ideas: List<IdeaEntity> = emptyList(),
    val projects: List<ProjectWithNodes> = emptyList(),
    val activeTab: Int = 0, // 0 = Ideas, 1 = Multi-node Projects
    val selectedCategory: String = "ALL",
    val searchQuery: String = "",
    val previewIdeaDto: IdeaDto? = null,
    val previewIdeaEntity: IdeaEntity? = null,
    val previewProjectDto: ProjectDto? = null,
    val selectedProjectForGraph: ProjectWithNodes? = null,
    val isIdeaSheetVisible: Boolean = false,
    val isProjectSheetVisible: Boolean = false,
    val isGraphDialogVisible: Boolean = false,
    val isManualPasteDialogOpen: Boolean = false,
    val isCreateManualBottomSheetVisible: Boolean = false,
    val isSyncingGithub: Boolean = false,
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class IdeaBoardViewModel(
    private val repository: IdeaRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(0)
    private val _selectedCategory = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _previewIdeaDto = MutableStateFlow<IdeaDto?>(null)
    private val _previewIdeaEntity = MutableStateFlow<IdeaEntity?>(null)
    private val _previewProjectDto = MutableStateFlow<ProjectDto?>(null)
    private val _selectedProjectForGraph = MutableStateFlow<ProjectWithNodes?>(null)
    private val _isIdeaSheetVisible = MutableStateFlow(false)
    private val _isProjectSheetVisible = MutableStateFlow(false)
    private val _isGraphDialogVisible = MutableStateFlow(false)
    private val _isManualPasteDialogOpen = MutableStateFlow(false)
    private val _isCreateManualBottomSheetVisible = MutableStateFlow(false)
    private val _isSyncingGithub = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val json = Json { prettyPrint = true }

    private val _ideas = combine(_selectedCategory, _searchQuery) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        val trimmed = query.trim()
        when {
            trimmed.isNotBlank() -> repository.searchIdeas(trimmed)
            category != "ALL" && category != "MIND" -> repository.getIdeasByCategory(category)
            else -> repository.getAllIdeas()
        }
    }

    private val _projects = combine(_selectedCategory, _searchQuery) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        val trimmed = query.trim()
        when {
            trimmed.isNotBlank() -> repository.searchProjects(trimmed)
            else -> repository.getAllProjects()
        }
    }

    val uiState: StateFlow<IdeaBoardUiState> = combine(
        combine(_ideas, _projects, _activeTab) { ideas, projects, tab ->
            Triple(ideas, projects, tab)
        },
        combine(_selectedCategory, _searchQuery, _previewIdeaDto, _previewIdeaEntity) { cat, q, dto, entity ->
            Tuple4(cat, q, dto, entity)
        },
        combine(_previewProjectDto, _selectedProjectForGraph, _isIdeaSheetVisible, _isProjectSheetVisible) { pDto, pGraph, isIdeaSheet, isProjSheet ->
            Tuple4(pDto, pGraph, isIdeaSheet, isProjSheet)
        },
        combine(_isGraphDialogVisible, _isManualPasteDialogOpen, _isCreateManualBottomSheetVisible, _isSyncingGithub) { isGraph, isDialog, isCreate, isSync ->
            Tuple4(isGraph, isDialog, isCreate, isSync)
        },
        _snackbarMessage
    ) { (ideas, projects, tab), (cat, q, dto, entity), (pDto, pGraph, isIdeaSheet, isProjSheet), (isGraph, isDialog, isCreate, isSync), msg ->
        IdeaBoardUiState(
            ideas = ideas,
            projects = projects,
            activeTab = tab,
            selectedCategory = cat,
            searchQuery = q,
            previewIdeaDto = dto,
            previewIdeaEntity = entity,
            previewProjectDto = pDto,
            selectedProjectForGraph = pGraph,
            isIdeaSheetVisible = isIdeaSheet,
            isProjectSheetVisible = isProjSheet,
            isGraphDialogVisible = isGraph,
            isManualPasteDialogOpen = isDialog,
            isCreateManualBottomSheetVisible = isCreate,
            isSyncingGithub = isSync,
            snackbarMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IdeaBoardUiState()
    )

    fun onTabSelected(index: Int) {
        _activeTab.value = index
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun connectRepoToProject(projectId: String, repoInput: String) {
        viewModelScope.launch {
            try {
                val slug = GithubLogService.extractRepoSlug(repoInput)
                    ?: throw IllegalArgumentException("Invalid repo format. Use owner/repo or github.com URL.")
                repository.linkRepoToProject(projectId, slug)
                _snackbarMessage.value = "GitHub repo linked: $slug"
                syncGithubCommits(projectId, slug)
            } catch (e: Exception) {
                _snackbarMessage.value = "Error linking repo: ${e.localizedMessage}"
            }
        }
    }

    fun unlinkRepoFromProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.unlinkRepoFromProject(projectId)
                _snackbarMessage.value = "GitHub repo unlinked."
            } catch (e: Exception) {
                _snackbarMessage.value = "Error unlinking repo: ${e.localizedMessage}"
            }
        }
    }

    fun syncGithubCommits(projectId: String, repoSlug: String) {
        viewModelScope.launch {
            _isSyncingGithub.value = true
            try {
                val commits = repository.fetchAndSyncGithubCommits(projectId = projectId, repoSlug = repoSlug)
                _snackbarMessage.value = "Synced ${commits.size} commits from GitHub!"
            } catch (e: Exception) {
                _snackbarMessage.value = "GitHub sync error: ${e.localizedMessage}"
            } finally {
                _isSyncingGithub.value = false
            }
        }
    }

    fun pasteAndConnectClipboardRepo(context: Context, projectId: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            val text = clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString() ?: ""
            val slug = GithubLogService.extractRepoSlug(text)
            if (slug != null) {
                connectRepoToProject(projectId, slug)
            } else {
                _snackbarMessage.value = "No valid GitHub URL or owner/repo found on clipboard."
            }
        } catch (e: Exception) {
            _snackbarMessage.value = "Error reading clipboard: ${e.localizedMessage}"
        }
    }

    fun scanClipboard(context: Context, hapticFeedback: HapticFeedback? = null) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                parseRawText(text, hapticFeedback)
            } else {
                _snackbarMessage.value = "Clipboard is empty or contains no readable text."
            }
        } catch (e: Exception) {
            _snackbarMessage.value = "Error reading clipboard: ${e.localizedMessage}"
        }
    }

    fun parseRawText(rawText: String?, hapticFeedback: HapticFeedback? = null) {
        when (val result = repository.parseClipboardText(rawText)) {
            is ParseResult.IdeaSuccess -> {
                _previewIdeaDto.value = result.dto
                _previewIdeaEntity.value = result.entity
                _isIdeaSheetVisible.value = true
                try {
                    hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (_: Exception) {}
            }
            is ParseResult.ProjectSuccess -> {
                _previewProjectDto.value = result.dto
                _isProjectSheetVisible.value = true
                try {
                    hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (_: Exception) {}
            }
            is ParseResult.Error -> {
                _snackbarMessage.value = result.message
            }
        }
    }

    fun copyIdeaAsJson(context: Context, idea: IdeaEntity, hapticFeedback: HapticFeedback? = null) {
        try {
            val dto = IdeaDto(
                schemaVersion = 1,
                title = idea.title,
                summary = idea.summary,
                category = idea.category,
                tags = idea.tags,
                firstStep = idea.firstStep,
                keyPoints = idea.keyPoints,
                createdAt = DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", Date(idea.savedAt)).toString()
            )
            val jsonString = json.encodeToString(dto)
            val markdownWrapped = "```json\n$jsonString\n```"

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("AI Idea JSON", markdownWrapped)
            clipboard?.setPrimaryClip(clip)

            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}

            _snackbarMessage.value = "Idea JSON copied to clipboard for AI!"
        } catch (e: Exception) {
            _snackbarMessage.value = "Error copying JSON: ${e.localizedMessage}"
        }
    }

    fun copyProjectAsJson(context: Context, projectWithNodes: ProjectWithNodes, hapticFeedback: HapticFeedback? = null) {
        try {
            val project = projectWithNodes.project
            val nodes = projectWithNodes.nodes

            val dto = ProjectDto(
                schemaVersion = 2,
                projectId = project.projectId,
                projectTitle = project.projectTitle,
                projectDescription = project.projectDescription,
                githubRepo = project.githubRepo,
                category = project.category,
                exportType = project.exportType,
                currentHead = CurrentHeadDto(
                    nextMicroStep = project.nextMicroStep,
                    status = project.status
                ),
                nodes = nodes.map { node ->
                    NodeDto(
                        nodeId = node.nodeId,
                        type = node.type,
                        tag = node.tag,
                        title = node.title,
                        detail = node.detail,
                        timestamp = node.timestamp
                    )
                }
            )
            val jsonString = json.encodeToString(dto)
            val markdownWrapped = "```json\n$jsonString\n```"

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("AI Project JSON", markdownWrapped)
            clipboard?.setPrimaryClip(clip)

            try {
                hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}

            _snackbarMessage.value = "Project JSON copied to clipboard for AI!"
        } catch (e: Exception) {
            _snackbarMessage.value = "Error copying JSON: ${e.localizedMessage}"
        }
    }

    fun savePreviewIdea() {
        val entity = _previewIdeaEntity.value ?: return
        viewModelScope.launch {
            try {
                repository.insertIdea(entity)
                _isIdeaSheetVisible.value = false
                _previewIdeaDto.value = null
                _previewIdeaEntity.value = null
                _snackbarMessage.value = "Idea saved successfully!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error saving idea: ${e.localizedMessage}"
            }
        }
    }

    fun savePreviewProject() {
        val dto = _previewProjectDto.value ?: return
        viewModelScope.launch {
            try {
                repository.saveProjectDto(dto)
                _isProjectSheetVisible.value = false
                val savedDto = _previewProjectDto.value
                _previewProjectDto.value = null
                _activeTab.value = 1 // Switch to Projects tab
                _snackbarMessage.value = "Multi-node project saved successfully!"

                // Auto-fetch commits if githubRepo is specified
                if (!savedDto?.githubRepo.isNullOrBlank()) {
                    syncGithubCommits(savedDto.projectId, savedDto.githubRepo)
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error saving project: ${e.localizedMessage}"
            }
        }
    }

    fun saveManualIdea(
        title: String,
        summary: String,
        category: String,
        tagsString: String,
        firstStep: String?,
        keyPointsString: String
    ) {
        if (title.isBlank() && summary.isBlank()) {
            _snackbarMessage.value = "Please enter at least a title or summary!"
            return
        }

        val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val keyPoints = keyPointsString.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }

        val entity = IdeaEntity(
            title = title.ifBlank { "Untitled Idea" },
            summary = summary.ifBlank { "No description provided." },
            category = category.trim().uppercase().ifBlank { "OTHER" },
            tags = tags,
            firstStep = firstStep?.takeIf { it.isNotBlank() },
            keyPoints = keyPoints,
            isFirstStepDone = false,
            savedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                repository.insertIdea(entity)
                _isCreateManualBottomSheetVisible.value = false
                _activeTab.value = 0
                _snackbarMessage.value = "New idea created successfully!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error saving idea: ${e.localizedMessage}"
            }
        }
    }

    fun saveManualProject(
        projectTitle: String,
        projectDescription: String?,
        githubRepo: String?,
        category: String,
        nextMicroStep: String?,
        initialNodeTitle: String,
        initialNodeType: String,
        initialNodeTag: String,
        initialNodeDetail: String?
    ) {
        if (projectTitle.isBlank()) {
            _snackbarMessage.value = "Please enter a project title!"
            return
        }

        val pid = "proj-" + UUID.randomUUID().toString().take(8)
        val cleanRepo = githubRepo?.takeIf { it.isNotBlank() }?.let { GithubLogService.extractRepoSlug(it) }

        val dto = ProjectDto(
            schemaVersion = 2,
            projectId = pid,
            projectTitle = projectTitle,
            projectDescription = projectDescription?.takeIf { it.isNotBlank() },
            githubRepo = cleanRepo,
            category = category.trim().uppercase().ifBlank { "OTHER" },
            exportType = "INIT",
            currentHead = CurrentHeadDto(
                nextMicroStep = nextMicroStep?.takeIf { it.isNotBlank() },
                status = "ACTIVE"
            ),
            nodes = listOf(
                NodeDto(
                    nodeId = "node-1",
                    type = initialNodeType.ifBlank { "DECISION" },
                    tag = initialNodeTag.ifBlank { "INIT" },
                    title = initialNodeTitle.ifBlank { "Project Started" },
                    detail = initialNodeDetail?.takeIf { it.isNotBlank() },
                    timestamp = DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", Date()).toString()
                )
            )
        )

        viewModelScope.launch {
            try {
                repository.saveProjectDto(dto)
                _isCreateManualBottomSheetVisible.value = false
                _activeTab.value = 1
                _snackbarMessage.value = "New project created successfully!"

                if (!cleanRepo.isNullOrBlank()) {
                    syncGithubCommits(pid, cleanRepo)
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error saving project: ${e.localizedMessage}"
            }
        }
    }

    fun dismissPreviewBottomSheet() {
        _isIdeaSheetVisible.value = false
        _isProjectSheetVisible.value = false
        _previewIdeaDto.value = null
        _previewIdeaEntity.value = null
        _previewProjectDto.value = null
    }

    fun openProjectGraph(projectWithNodes: ProjectWithNodes) {
        _selectedProjectForGraph.value = projectWithNodes
        _isGraphDialogVisible.value = true
    }

    fun dismissProjectGraph() {
        _isGraphDialogVisible.value = false
        _selectedProjectForGraph.value = null
    }

    fun toggleFirstStepDone(ideaId: Long, isDone: Boolean, hapticFeedback: HapticFeedback? = null) {
        viewModelScope.launch {
            try {
                repository.updateFirstStepDone(ideaId, isDone)
                if (isDone) {
                    try {
                        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error updating status: ${e.localizedMessage}"
            }
        }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            try {
                repository.deleteIdea(idea)
                _snackbarMessage.value = "Idea deleted."
            } catch (e: Exception) {
                _snackbarMessage.value = "Error deleting idea: ${e.localizedMessage}"
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectId)
                _snackbarMessage.value = "Project deleted."
            } catch (e: Exception) {
                _snackbarMessage.value = "Error deleting project: ${e.localizedMessage}"
            }
        }
    }

    fun openManualPasteDialog() {
        _isManualPasteDialogOpen.value = true
    }

    fun dismissManualPasteDialog() {
        _isManualPasteDialogOpen.value = false
    }

    fun openCreateManualBottomSheet() {
        _isCreateManualBottomSheetVisible.value = true
    }

    fun dismissCreateManualBottomSheet() {
        _isCreateManualBottomSheetVisible.value = false
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    class Factory(private val repository: IdeaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IdeaBoardViewModel(repository) as T
        }
    }
}
