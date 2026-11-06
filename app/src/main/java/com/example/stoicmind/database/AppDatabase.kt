package com.example.stoicmind.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stoicmind.database.dao.CommunityDao
import com.example.stoicmind.database.dao.JournalDao
import com.example.stoicmind.database.dao.MoodDao
import com.example.stoicmind.database.entity.CommunityEntity
import com.example.stoicmind.database.entity.JournalEntity
import com.example.stoicmind.database.entity.MoodEntity

@Database(
    entities = [JournalEntity::class, MoodEntity::class, CommunityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun moodDao(): MoodDao
    abstract fun communityDao(): CommunityDao

    companion object {
        const val DATABASE_NAME = "stoicmind_db"
    }
}
