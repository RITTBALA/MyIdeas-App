package hu.bme.aut.android.myideas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY savedAt DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE category = :category ORDER BY savedAt DESC")
    fun getIdeasByCategory(category: String): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchIdeas(query: String): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Query("UPDATE ideas SET isFirstStepDone = :isDone WHERE id = :id")
    suspend fun updateFirstStepDone(id: Long, isDone: Boolean)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteIdeaById(id: Long)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)
}
