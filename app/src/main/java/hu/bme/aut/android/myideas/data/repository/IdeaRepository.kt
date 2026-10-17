package hu.bme.aut.android.myideas.data.repository

import hu.bme.aut.android.myideas.data.local.GitCommitEntity
import hu.bme.aut.android.myideas.data.local.IdeaDao
import hu.bme.aut.android.myideas.data.local.IdeaEntity
import hu.bme.aut.android.myideas.data.local.ProjectDao
import hu.bme.aut.android.myideas.data.local.ProjectWithNodes
import hu.bme.aut.android.myideas.data.model.ProjectDto
import hu.bme.aut.android.myideas.data.network.GithubLogService
import hu.bme.aut.android.myideas.data.parser.ClipboardParser
import hu.bme.aut.android.myideas.data.parser.ParseResult
import kotlinx.coroutines.flow.Flow

class IdeaRepository(
    private val ideaDao: IdeaDao,
    private val projectDao: ProjectDao,
    private val clipboardParser: ClipboardParser = ClipboardParser(),
    private val githubLogService: GithubLogService = GithubLogService()
) {
    // Idea Operations
    fun getAllIdeas(): Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()

    fun getIdeasByCategory(category: String): Flow<List<IdeaEntity>> = ideaDao.getIdeasByCategory(category)

    fun searchIdeas(query: String): Flow<List<IdeaEntity>> = ideaDao.searchIdeas(query)

    suspend fun insertIdea(entity: IdeaEntity): Long {
        return ideaDao.insertIdea(entity)
    }

    suspend fun updateFirstStepDone(id: Long, isDone: Boolean) {
        ideaDao.updateFirstStepDone(id, isDone)
    }

    suspend fun deleteIdea(idea: IdeaEntity) {
        ideaDao.deleteIdea(idea)
    }

    // Project Operations (Multi-node Git Commit Graph)
    fun getAllProjects(): Flow<List<ProjectWithNodes>> = projectDao.getAllProjectsWithNodes()

    fun getProjectById(projectId: String): Flow<ProjectWithNodes?> = projectDao.getProjectWithNodesById(projectId)

    fun searchProjects(query: String): Flow<List<ProjectWithNodes>> = projectDao.searchProjects(query)

    suspend fun saveProjectDto(dto: ProjectDto) {
        projectDao.upsertProjectWithNodes(dto)
    }

    suspend fun linkRepoToProject(projectId: String, repoInput: String) {
        val slug = GithubLogService.extractRepoSlug(repoInput) ?: throw IllegalArgumentException("Invalid GitHub repo format")
        projectDao.updateGithubRepo(projectId = projectId, repo = slug)
    }

    suspend fun unlinkRepoFromProject(projectId: String) {
        projectDao.updateGithubRepo(projectId = projectId, repo = null)
        projectDao.deleteCommitsForProject(projectId)
    }

    suspend fun fetchAndSyncGithubCommits(projectId: String, repoSlug: String, token: String? = null): List<GitCommitEntity> {
        val commits = githubLogService.fetchCommits(projectId = projectId, repoSlug = repoSlug, token = token)
        projectDao.syncGitCommits(projectId, commits)
        return commits
    }

    suspend fun updateProjectNextStep(projectId: String, nextStep: String?) {
        projectDao.updateNextMicroStep(projectId, nextStep)
    }

    suspend fun updateProjectStatus(projectId: String, status: String) {
        projectDao.updateProjectStatus(projectId, status)
    }

    suspend fun deleteProject(projectId: String) {
        projectDao.deleteProjectById(projectId)
    }

    // Parser
    fun parseClipboardText(text: String?): ParseResult {
        return clipboardParser.parse(text)
    }
}
