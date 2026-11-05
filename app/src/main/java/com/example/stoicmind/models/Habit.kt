package com.example.stoicmind.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

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
    @get:PropertyName("isActive") @field:PropertyName("isActive") @SerializedName("isActive")
    val isActive: Boolean = true,
    val colorHex: String = "#D1E8E2",
    @get:PropertyName("isCompleted") @field:PropertyName("isCompleted") @SerializedName("isCompleted")
    val isCompleted: Boolean = false
)
