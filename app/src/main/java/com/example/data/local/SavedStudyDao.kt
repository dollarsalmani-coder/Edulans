package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedStudyDao {
    @Query("SELECT * FROM saved_studies ORDER BY timestamp DESC")
    fun observeAllStudies(): Flow<List<SavedStudyEntity>>

    @Query(
        "SELECT * FROM saved_studies " +
            "WHERE objectTitle LIKE '%' || :query || '%' " +
            "OR category LIKE '%' || :query || '%' " +
            "OR summary LIKE '%' || :query || '%' " +
            "ORDER BY timestamp DESC"
    )
    fun searchStudies(query: String): Flow<List<SavedStudyEntity>>

    @Query("SELECT * FROM saved_studies WHERE id = :id LIMIT 1")
    suspend fun getStudyById(id: String): SavedStudyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudy(study: SavedStudyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(studies: List<SavedStudyEntity>)

    @Query("DELETE FROM saved_studies WHERE id = :id")
    suspend fun deleteStudyById(id: String)

    @Query("SELECT COUNT(*) FROM saved_studies")
    suspend fun countStudies(): Int
}
