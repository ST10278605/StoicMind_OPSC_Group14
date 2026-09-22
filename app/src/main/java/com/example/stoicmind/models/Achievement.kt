package com.example.stoicmind.models

data class Achievement(
    val id: String = "",
    val userId: String = "",
    val badgeName: String = "",
    val badgeDescription: String = "",
    val iconResId: Int = 0,
    val unlockedAt: Long = 0,
    val pointsEarned: Int = 0,
    val isUnlocked: Boolean = false
)