package com.example.stoicmind.models

data class Habit(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val frequency: String = "daily",
    val reminderTime: String = "",
    val completedDays: List<String> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isActive: Boolean = true
)