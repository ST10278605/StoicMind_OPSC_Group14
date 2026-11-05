package com.example.stoicmind.repository

import android.util.Log
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.database.dao.JournalDao
import com.example.stoicmind.database.dao.MoodDao
import com.example.stoicmind.database.entity.JournalEntity
import com.example.stoicmind.database.entity.MoodEntity
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.MoodCheckIn
import com.example.stoicmind.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncStatus {
    OFFLINE,
    SYNCING,
    SYNCED,
    ERROR
}

data class SyncState(
    val status: SyncStatus = SyncStatus.OFFLINE,
    val unsyncedJournalCount: Int = 0,
    val unsyncedMoodCount: Int = 0,
    val lastSyncTimestamp: Long = 0L,
    val message: String = ""
)

@Singleton
class SyncRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val moodDao: MoodDao,
    private val firebaseManager: FirebaseManager,
    private val networkMonitor: NetworkMonitor
) {
    private val tag = "SyncRepository"
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    init {
        repositoryScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    Log.d(tag, "Network connection detected. Triggering auto-sync.")
                    syncAll()
                } else {
                    updateUnsyncedCounts(SyncStatus.OFFLINE, "Offline mode active")
                }
            }
        }
    }

    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntry>> {
        return journalDao.getJournalEntriesFlow(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getMoodCheckInsFlow(userId: String): Flow<List<MoodCheckIn>> {
        return moodDao.getMoodCheckInsFlow(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveJournalEntry(entry: JournalEntry): Result<String> = withContext(Dispatchers.IO) {
        val entryId = entry.id.ifEmpty { UUID.randomUUID().toString() }
        val finalEntry = entry.copy(id = entryId)
        
        val isOnlineNow = networkMonitor.isOnline.value
        val entity = JournalEntity.fromModel(finalEntry, synced = false)
        
        // Always save locally to Room first (Offline first approach)
        journalDao.insertJournalEntry(entity)
        
        if (isOnlineNow) {
            try {
                _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING, message = "Syncing entry...")
                val result = firebaseManager.saveJournalEntry(finalEntry)
                if (result.isSuccess) {
                    val syncedEntity = entity.copy(isSynced = true)
                    journalDao.insertJournalEntry(syncedEntity)
                    updateUnsyncedCounts(SyncStatus.SYNCED, "Saved & Synced")
                    return@withContext Result.success(entryId)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed online sync for entry $entryId", e)
            }
        }
        
        updateUnsyncedCounts(SyncStatus.OFFLINE, "Saved locally (Offline)")
        return@withContext Result.success(entryId)
    }

    suspend fun saveMoodCheckIn(mood: MoodCheckIn): Result<String> = withContext(Dispatchers.IO) {
        val moodId = mood.id.ifEmpty { UUID.randomUUID().toString() }
        val finalMood = mood.copy(id = moodId)
        
        val isOnlineNow = networkMonitor.isOnline.value
        val entity = MoodEntity.fromModel(finalMood, synced = false)
        
        // Save locally to Room first
        moodDao.insertMoodCheckIn(entity)
        
        if (isOnlineNow) {
            try {
                val result = firebaseManager.saveMood(finalMood)
                if (result.isSuccess) {
                    val syncedEntity = entity.copy(isSynced = true)
                    moodDao.insertMoodCheckIn(syncedEntity)
                    updateUnsyncedCounts(SyncStatus.SYNCED, "Saved & Synced")
                    return@withContext Result.success(moodId)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed online sync for mood $moodId", e)
            }
        }
        
        updateUnsyncedCounts(SyncStatus.OFFLINE, "Saved locally (Offline)")
        return@withContext Result.success(moodId)
    }

    suspend fun syncAll(): Unit = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline.value) {
            updateUnsyncedCounts(SyncStatus.OFFLINE, "Device is offline")
            return@withContext
        }

        val unsyncedJournals = journalDao.getUnsyncedEntries()
        val unsyncedMoods = moodDao.getUnsyncedMoodCheckIns()

        if (unsyncedJournals.isEmpty() && unsyncedMoods.isEmpty()) {
            fetchRemoteData()
            updateUnsyncedCounts(SyncStatus.SYNCED, "All data up to date")
            return@withContext
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING, message = "Syncing unsynced data...")

        var syncedCount = 0

        // Sync unsynced journal entries
        for (journal in unsyncedJournals) {
            val model = journal.toModel()
            val result = firebaseManager.saveJournalEntry(model)
            if (result.isSuccess) {
                journalDao.markAsSynced(journal.id)
                syncedCount++
            }
        }

        // Sync unsynced mood check-ins
        for (mood in unsyncedMoods) {
            val model = mood.toModel()
            val result = firebaseManager.saveMood(model)
            if (result.isSuccess) {
                moodDao.markAsSynced(mood.id)
                syncedCount++
            }
        }

        // Fetch remote data into Room DB
        fetchRemoteData()

        updateUnsyncedCounts(SyncStatus.SYNCED, "Synced $syncedCount item(s)")
    }

    private suspend fun fetchRemoteData() {
        try {
            val userId = firebaseManager.getCurrentUserId() ?: return
            val remoteJournals = firebaseManager.getJournalEntries(userId)
            val journalEntities = remoteJournals.map { JournalEntity.fromModel(it, synced = true) }
            if (journalEntities.isNotEmpty()) {
                journalDao.insertJournalEntries(journalEntities)
            }

            val remoteMoods = firebaseManager.getMoods(userId)
            val moodEntities = remoteMoods.map { MoodEntity.fromModel(it, synced = true) }
            if (moodEntities.isNotEmpty()) {
                moodDao.insertMoodCheckIns(moodEntities)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching remote data", e)
        }
    }

    private suspend fun updateUnsyncedCounts(status: SyncStatus, message: String) {
        val unsyncedJournals = journalDao.getUnsyncedEntries().size
        val unsyncedMoods = moodDao.getUnsyncedMoodCheckIns().size

        _syncState.value = SyncState(
            status = status,
            unsyncedJournalCount = unsyncedJournals,
            unsyncedMoodCount = unsyncedMoods,
            lastSyncTimestamp = System.currentTimeMillis(),
            message = message
        )
    }
}
