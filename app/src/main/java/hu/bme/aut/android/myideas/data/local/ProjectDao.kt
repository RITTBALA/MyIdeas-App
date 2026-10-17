package hu.bme.aut.android.myideas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import hu.bme.aut.android.myideas.data.model.ProjectDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ProjectDao {

    @Transaction
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjectsWithNodes(): Flow<List<ProjectWithNodes>>

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectWithNodesById(projectId: String): Flow<ProjectWithNodes?>

    @Transaction
    @Query("SELECT * FROM projects WHERE projectTitle LIKE '%' || :query || '%' OR projectDescription LIKE '%' || :query || '%' OR githubRepo LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchProjects(query: String): Flow<List<ProjectWithNodes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommits(commits: List<GitCommitEntity>)

    @Query("DELETE FROM git_commits WHERE parentProjectId = :projectId")
    suspend fun deleteCommitsForProject(projectId: String)

    @Query("UPDATE projects SET githubRepo = :repo, defaultBranch = :defaultBranch, lastSyncedAt = :syncedAt, updatedAt = :updatedAt WHERE projectId = :projectId")
    suspend fun updateGithubRepo(
        projectId: String,
        repo: String?,
        defaultBranch: String = "main",
        syncedAt: Long? = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Transaction
    suspend fun syncGitCommits(projectId: String, commits: List<GitCommitEntity>) {
        deleteCommitsForProject(projectId)
        if (commits.isNotEmpty()) {
            insertCommits(commits)
        }
        updateGithubRepo(projectId = projectId, repo = getProjectByIdSync(projectId)?.githubRepo, syncedAt = System.currentTimeMillis())
    }

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    suspend fun getProjectByIdSync(projectId: String): ProjectEntity?

    @Query("UPDATE projects SET nextMicroStep = :nextMicroStep, updatedAt = :updatedAt WHERE projectId = :projectId")
    suspend fun updateNextMicroStep(projectId: String, nextMicroStep: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET status = :status, updatedAt = :updatedAt WHERE projectId = :projectId")
    suspend fun updateProjectStatus(projectId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Transaction
    suspend fun upsertProjectWithNodes(dto: ProjectDto) {
        val pid = dto.projectId.ifBlank { UUID.randomUUID().toString() }
        val projectEntity = ProjectEntity(
            projectId = pid,
            projectTitle = dto.projectTitle.ifBlank { "Untitled Project" },
            projectDescription = dto.projectDescription?.takeIf { it.isNotBlank() },
            githubRepo = dto.githubRepo?.takeIf { it.isNotBlank() },
            defaultBranch = "main",
            lastSyncedAt = if (!dto.githubRepo.isNullOrBlank()) System.currentTimeMillis() else null,
            category = dto.category.trim().uppercase().ifBlank { "OTHER" },
            exportType = dto.exportType,
            nextMicroStep = dto.currentHead?.nextMicroStep,
            status = dto.currentHead?.status?.uppercase() ?: "ACTIVE",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        insertProject(projectEntity)

        val nodeEntities = dto.nodes.mapIndexed { index, nodeDto ->
            NodeEntity(
                nodeId = nodeDto.nodeId.ifBlank { UUID.randomUUID().toString() },
                projectId = pid,
                type = nodeDto.type.uppercase(),
                tag = nodeDto.tag.ifBlank { "GENERAL" },
                title = nodeDto.title.ifBlank { "Node" },
                detail = nodeDto.detail,
                timestamp = nodeDto.timestamp,
                orderIndex = index
            )
        }
        if (nodeEntities.isNotEmpty()) {
            insertNodes(nodeEntities)
        }

        val commitEntities = dto.commits.map { commitDto ->
            val fullMsg = commitDto.message.ifBlank { "Git commit" }
            val lines = fullMsg.split("\n", limit = 2)
            val title = lines.firstOrNull()?.trim() ?: "Git commit"
            val body = lines.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

            GitCommitEntity(
                sha = commitDto.sha.ifBlank { UUID.randomUUID().toString().take(7) },
                parentProjectId = pid,
                messageTitle = title,
                messageBody = body,
                authorName = commitDto.author.ifBlank { "Developer" },
                timestamp = commitDto.timestamp.ifBlank { "2026-10-17T22:00:00Z" },
                webUrl = commitDto.htmlUrl ?: "https://github.com/${dto.githubRepo ?: ""}"
            )
        }
        if (commitEntities.isNotEmpty()) {
            insertCommits(commitEntities)
        }
    }
}
