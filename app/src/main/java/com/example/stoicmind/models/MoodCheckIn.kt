package com.example.stoicmind.models

data class MoodCheckIn(
    val id: String = "",
    val userId: String = "",
    val mood: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)