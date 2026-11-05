package com.example.stoicmind.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stoicmind.database.entity.CommunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {

    @Query("SELECT * FROM community_posts ORDER BY date DESC")
    fun getCommunityPostsFlow(): Flow<List<CommunityEntity>>

    @Query("SELECT * FROM community_posts ORDER BY date DESC")
    suspend fun getCommunityPosts(): List<CommunityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPost(post: CommunityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPosts(posts: List<CommunityEntity>): List<Long>

    @Query("UPDATE community_posts SET likesCount = likesCount + :delta, isLikedByMe = :isLiked WHERE id = :id")
    suspend fun updateLikeStatus(id: String, isLiked: Boolean, delta: Int): Int
}
