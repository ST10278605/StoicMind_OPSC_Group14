package com.example.stoicmind.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stoicmind.models.CommunityPost

@Entity(tableName = "community_posts")
data class CommunityEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val authorName: String,
    val title: String,
    val content: String,
    val category: String,
    val date: Long,
    val likesCount: Int,
    val isLikedByMe: Boolean = false,
    val isSynced: Boolean = true
) {
    fun toModel(): CommunityPost {
        return CommunityPost(
            id = id,
            userId = userId,
            authorName = authorName,
            title = title,
            content = content,
            category = category,
            date = date,
            likesCount = likesCount,
            isLikedByMe = isLikedByMe,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromModel(post: CommunityPost): CommunityEntity {
            return CommunityEntity(
                id = post.id,
                userId = post.userId,
                authorName = post.authorName,
                title = post.title,
                content = post.content,
                category = post.category,
                date = post.date,
                likesCount = post.likesCount,
                isLikedByMe = post.isLikedByMe,
                isSynced = post.isSynced
            )
        }
    }
}
