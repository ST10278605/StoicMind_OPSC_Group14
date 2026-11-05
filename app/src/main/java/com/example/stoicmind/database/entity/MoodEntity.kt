package com.example.stoicmind.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stoicmind.models.MoodCheckIn

@Entity(tableName = "mood_checkins")
data class MoodEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val mood: String,
    val note: String,
    val date: Long,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toModel(): MoodCheckIn {
        return MoodCheckIn(
            id = id,
            userId = userId,
            mood = mood,
            note = note,
            date = date,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromModel(moodCheckIn: MoodCheckIn, synced: Boolean = moodCheckIn.isSynced): MoodEntity {
            return MoodEntity(
                id = moodCheckIn.id,
                userId = moodCheckIn.userId,
                mood = moodCheckIn.mood,
                note = moodCheckIn.note,
                date = moodCheckIn.date,
                isSynced = synced,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
