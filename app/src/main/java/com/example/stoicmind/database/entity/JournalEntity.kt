package com.example.stoicmind.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stoicmind.models.JournalEntry

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mood: String,
    val date: Long,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toModel(): JournalEntry {
        return JournalEntry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            mood = mood,
            date = date,
            tags = emptyList(),
            isSynced = isSynced
        )
    }

    companion object {
        fun fromModel(entry: JournalEntry, synced: Boolean = entry.isSynced): JournalEntity {
            return JournalEntity(
                id = entry.id,
                userId = entry.userId,
                title = entry.title,
                content = entry.content,
                mood = entry.mood,
                date = entry.date,
                isSynced = synced,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
