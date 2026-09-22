package com.example.stoicmind.models

data class JournalEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val mood: String = "",
    val date: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false
)