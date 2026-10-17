package hu.bme.aut.android.myideas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "project_nodes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class NodeEntity(
    @PrimaryKey val nodeId: String,
    val projectId: String,
    val type: String, // DECISION | TASK_DONE | MILESTONE | OPEN_QUESTION
    val tag: String,
    val title: String,
    val detail: String?,
    val timestamp: String,
    val orderIndex: Int = 0
)
