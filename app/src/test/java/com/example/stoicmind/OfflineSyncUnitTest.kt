package com.example.stoicmind

import com.example.stoicmind.database.entity.JournalEntity
import com.example.stoicmind.database.entity.MoodEntity
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.MoodCheckIn
import com.example.stoicmind.repository.SyncState
import com.example.stoicmind.repository.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineSyncUnitTest {

    @Test
    fun journalEntityConversionToAndFromModelPreservesData() {
        val originalModel = JournalEntry(
            id = "test_id_101",
            userId = "user_456",
            title = "Stoic Reflection",
            content = "Amor Fati - love your fate.",
            mood = "😌 Calm",
            date = 1700000000000L,
            isSynced = false
        )

        val entity = JournalEntity.fromModel(originalModel, synced = false)
        assertEquals("test_id_101", entity.id)
        assertEquals("user_456", entity.userId)
        assertEquals("Stoic Reflection", entity.title)
        assertEquals("Amor Fati - love your fate.", entity.content)
        assertEquals("😌 Calm", entity.mood)
        assertFalse(entity.isSynced)

        val convertedModel = entity.toModel()
        assertEquals(originalModel.id, convertedModel.id)
        assertEquals(originalModel.userId, convertedModel.userId)
        assertEquals(originalModel.title, convertedModel.title)
        assertEquals(originalModel.content, convertedModel.content)
        assertEquals(originalModel.mood, convertedModel.mood)
        assertEquals(originalModel.date, convertedModel.date)
        assertEquals(originalModel.isSynced, convertedModel.isSynced)
    }

    @Test
    fun moodEntityConversionToAndFromModelPreservesData() {
        val originalMood = MoodCheckIn(
            id = "mood_999",
            userId = "user_789",
            mood = "😊 Happy",
            note = "Felt great after morning meditation.",
            date = 1700000500000L,
            isSynced = false
        )

        val entity = MoodEntity.fromModel(originalMood, synced = false)
        assertEquals("mood_999", entity.id)
        assertEquals("user_789", entity.userId)
        assertEquals("😊 Happy", entity.mood)
        assertEquals("Felt great after morning meditation.", entity.note)
        assertFalse(entity.isSynced)

        val convertedMood = entity.toModel()
        assertEquals(originalMood.id, convertedMood.id)
        assertEquals(originalMood.userId, convertedMood.userId)
        assertEquals(originalMood.mood, convertedMood.mood)
        assertEquals(originalMood.note, convertedMood.note)
        assertEquals(originalMood.isSynced, convertedMood.isSynced)
    }

    @Test
    fun syncStateTrackingUnsyncedCountsCorrectly() {
        val state = SyncState(
            status = SyncStatus.OFFLINE,
            unsyncedJournalCount = 3,
            unsyncedMoodCount = 2,
            message = "Offline mode active"
        )

        assertEquals(SyncStatus.OFFLINE, state.status)
        assertEquals(3, state.unsyncedJournalCount)
        assertEquals(2, state.unsyncedMoodCount)
        assertEquals("Offline mode active", state.message)
    }

    @Test
    fun journalEntityMarkedSyncedUpdatesFlag() {
        val unsyncedEntity = JournalEntity(
            id = "j1",
            userId = "u1",
            title = "T",
            content = "C",
            mood = "M",
            date = 1000L,
            isSynced = false
        )

        val syncedEntity = unsyncedEntity.copy(isSynced = true)
        assertTrue(syncedEntity.isSynced)
        val model = syncedEntity.toModel()
        assertTrue(model.isSynced)
    }
}
