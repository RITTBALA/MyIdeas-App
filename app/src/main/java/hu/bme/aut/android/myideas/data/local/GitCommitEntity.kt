package hu.bme.aut.android.myideas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "git_commits",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["parentProjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentProjectId")]
)
data class GitCommitEntity(
    @PrimaryKey val sha: String, // Full commit SHA
    val parentProjectId: String,
    val messageTitle: String,
    val messageBody: String? = null,
    val authorName: String,
    val timestamp: String,
    val webUrl: String
)
