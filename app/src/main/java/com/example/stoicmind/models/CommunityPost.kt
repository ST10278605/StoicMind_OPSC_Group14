package com.example.stoicmind.models

data class CommunityPost(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Stoic Wisdom",
    val date: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isSynced: Boolean = true
)
