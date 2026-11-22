package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadshotDao {
    @Query("SELECT * FROM headshots ORDER BY createdAt DESC")
    fun observeAllHeadshots(): Flow<List<HeadshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeadshot(headshot: HeadshotEntity): Long

    @Query("UPDATE headshots SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM headshots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM headshots WHERE sourceImagePath = :path")
    suspend fun countReferencesToSource(path: String): Int
}
