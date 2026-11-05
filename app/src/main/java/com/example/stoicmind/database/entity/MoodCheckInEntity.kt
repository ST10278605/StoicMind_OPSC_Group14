package com.example.stoicmind.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stoicmind.models.MoodCheckIn

@Entity(tableName = "mood_checkins")
data class MoodCheckInEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mood: String,
    val note: String,
    val date: Long,
    val isSynced: Boolean
) {
    fun toDomainModel(): MoodCheckIn {
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
        fun fromDomainModel(model: MoodCheckIn): MoodCheckInEntity {
            return MoodCheckInEntity(
                id = model.id,
                userId = model.userId,
                mood = model.mood,
                note = model.note,
                date = model.date,
                isSynced = model.isSynced
            )
        }
    }
}
