package com.example.stoicmind.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stoicmind.database.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    suspend fun getJournalEntries(userId: String): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE isSynced = 0")
    suspend fun getUnsyncedJournalEntries(): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    @Query("UPDATE journal_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
