package com.example.stoicmind.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stoicmind.database.dao.JournalDao
import com.example.stoicmind.database.dao.MoodDao
import com.example.stoicmind.database.entity.JournalEntryEntity
import com.example.stoicmind.database.entity.MoodCheckInEntity

@Database(
    entities = [JournalEntryEntity::class, MoodCheckInEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun moodDao(): MoodDao

    companion object {
        const val DATABASE_NAME = "stoic_mind_db"
    }
}
