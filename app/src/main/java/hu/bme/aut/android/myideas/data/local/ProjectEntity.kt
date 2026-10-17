package hu.bme.aut.android.myideas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val projectId: String,
    val projectTitle: String,
    val projectDescription: String? = null,
    val githubRepo: String? = null,
    val defaultBranch: String = "main",
    val lastSyncedAt: Long? = null,
    val category: String,
    val exportType: String,
    val nextMicroStep: String?,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
