package com.example.stoicmind.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stoicmind.database.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    suspend fun getJournalEntries(userId: String): List<JournalEntity>

    @Query("SELECT * FROM journal_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<JournalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntries(entries: List<JournalEntity>): List<Long>

    @Query("UPDATE journal_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: String): Int
}
