package hu.bme.aut.android.myideas.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithNodes(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val nodes: List<NodeEntity>,
    @Relation(
        parentColumn = "projectId",
        entityColumn = "parentProjectId"
    )
    val commits: List<GitCommitEntity> = emptyList()
)
