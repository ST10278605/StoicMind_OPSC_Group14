package com.example.stoicmind.di

import android.content.Context
import androidx.room.Room
import com.example.stoicmind.database.AppDatabase
import com.example.stoicmind.database.dao.CommunityDao
import com.example.stoicmind.database.dao.JournalDao
import com.example.stoicmind.database.dao.MoodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(appDatabase: AppDatabase): JournalDao {
        return appDatabase.journalDao()
    }

    @Provides
    @Singleton
    fun provideMoodDao(appDatabase: AppDatabase): MoodDao {
        return appDatabase.moodDao()
    }

    @Provides
    @Singleton
    fun provideCommunityDao(appDatabase: AppDatabase): CommunityDao {
        return appDatabase.communityDao()
    }
}
