package hu.bme.aut.android.myideas.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    @SerialName("schema_version") val schemaVersion: Int = 2,
    @SerialName("project_id") val projectId: String = "",
    @SerialName("project_title") val projectTitle: String = "",
    @SerialName("project_description") val projectDescription: String? = null,
    @SerialName("github_repo") val githubRepo: String? = null,
    val category: String = "EGYÉB",
    @SerialName("export_type") val exportType: String = "INIT",
    @SerialName("current_head") val currentHead: CurrentHeadDto? = null,
    val nodes: List<NodeDto> = emptyList(),
    val commits: List<GitCommitDto> = emptyList()
)

@Serializable
data class CurrentHeadDto(
    @SerialName("next_micro_step") val nextMicroStep: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class NodeDto(
    @SerialName("node_id") val nodeId: String = "",
    val type: String = "TASK_DONE",
    val tag: String = "",
    val title: String = "",
    val detail: String? = null,
    val timestamp: String = ""
)

@Serializable
data class GitCommitDto(
    val sha: String = "",
    val message: String = "",
    val author: String = "",
    val timestamp: String = "",
    @SerialName("html_url") val htmlUrl: String? = null
)
