package com.example.stoicmind.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stoicmind.models.JournalEntry

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mood: String,
    val date: Long,
    val tags: String,
    val isSynced: Boolean
) {
    fun toDomainModel(): JournalEntry {
        return JournalEntry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            mood = mood,
            date = date,
            tags = if (tags.isBlank()) emptyList() else tags.split(","),
            isSynced = isSynced
        )
    }

    companion object {
        fun fromDomainModel(model: JournalEntry): JournalEntryEntity {
            return JournalEntryEntity(
                id = model.id,
                userId = model.userId,
                title = model.title,
                content = model.content,
                mood = model.mood,
                date = model.date,
                tags = model.tags.joinToString(","),
                isSynced = model.isSynced
            )
        }
    }
}
