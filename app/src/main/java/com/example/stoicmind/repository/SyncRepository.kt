package com.example.stoicmind.repository

import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.database.dao.JournalDao
import com.example.stoicmind.database.dao.MoodDao
import com.example.stoicmind.database.entity.JournalEntryEntity
import com.example.stoicmind.database.entity.MoodCheckInEntity
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.MoodCheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncStatus {
    OFFLINE,
    SYNCING,
    SYNCED,
    ERROR
}

data class SyncState(
    val status: SyncStatus = SyncStatus.SYNCED,
    val unsyncedJournalCount: Int = 0,
    val message: String = ""
)

@Singleton
class SyncRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val moodDao: MoodDao,
    private val firebaseManager: FirebaseManager
) {

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntry>> {
        return journalDao.getJournalEntriesFlow(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveJournalEntry(entry: JournalEntry): Result<String> {
        val entryId = if (entry.id.isBlank()) "journal_${System.currentTimeMillis()}" else entry.id
        val entryToSave = entry.copy(id = entryId)

        return try {
            val cloudResult = firebaseManager.saveJournalEntry(entryToSave)
            if (cloudResult.isSuccess) {
                val syncedEntity = JournalEntryEntity.fromDomainModel(entryToSave.copy(isSynced = true))
                journalDao.insertJournalEntry(syncedEntity)
                _syncState.value = SyncState(status = SyncStatus.SYNCED)
                Result.success(entryId)
            } else {
                val unsyncedEntity = JournalEntryEntity.fromDomainModel(entryToSave.copy(isSynced = false))
                journalDao.insertJournalEntry(unsyncedEntity)
                _syncState.value = SyncState(status = SyncStatus.OFFLINE, unsyncedJournalCount = 1)
                Result.success(entryId)
            }
        } catch (e: Exception) {
            val unsyncedEntity = JournalEntryEntity.fromDomainModel(entryToSave.copy(isSynced = false))
            journalDao.insertJournalEntry(unsyncedEntity)
            _syncState.value = SyncState(status = SyncStatus.OFFLINE, message = e.message ?: "Saved offline")
            Result.success(entryId)
        }
    }

    suspend fun saveMoodCheckIn(mood: MoodCheckIn): Result<String> {
        val moodId = if (mood.id.isBlank()) "mood_${System.currentTimeMillis()}" else mood.id
        val moodToSave = mood.copy(id = moodId)

        return try {
            val unsyncedEntity = MoodCheckInEntity.fromDomainModel(moodToSave.copy(isSynced = false))
            moodDao.insertMoodCheckIn(unsyncedEntity)
            _syncState.value = SyncState(status = SyncStatus.SYNCED)
            Result.success(moodId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAll() {
        _syncState.value = SyncState(status = SyncStatus.SYNCING)
        try {
            val unsyncedEntries = journalDao.getUnsyncedJournalEntries()
            for (entity in unsyncedEntries) {
                val cloudResult = firebaseManager.saveJournalEntry(entity.toDomainModel())
                if (cloudResult.isSuccess) {
                    journalDao.markAsSynced(entity.id)
                }
            }
            _syncState.value = SyncState(status = SyncStatus.SYNCED)
        } catch (e: Exception) {
            _syncState.value = SyncState(status = SyncStatus.ERROR, message = e.message ?: "Sync failed")
        }
    }
}
