package hu.bme.aut.android.myideas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String,
    val category: String,
    val tags: List<String>,
    val firstStep: String?,
    val keyPoints: List<String>,
    val isFirstStepDone: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
