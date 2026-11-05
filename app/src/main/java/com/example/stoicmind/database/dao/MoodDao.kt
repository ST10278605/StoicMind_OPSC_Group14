package com.example.stoicmind.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stoicmind.database.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM mood_checkins WHERE userId = :userId ORDER BY date DESC")
    fun getMoodCheckInsFlow(userId: String): Flow<List<MoodEntity>>

    @Query("SELECT * FROM mood_checkins WHERE userId = :userId ORDER BY date DESC")
    suspend fun getMoodCheckIns(userId: String): List<MoodEntity>

    @Query("SELECT * FROM mood_checkins WHERE isSynced = 0")
    suspend fun getUnsyncedMoodCheckIns(): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodCheckIn(mood: MoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodCheckIns(moods: List<MoodEntity>): List<Long>

    @Query("UPDATE mood_checkins SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int
}
