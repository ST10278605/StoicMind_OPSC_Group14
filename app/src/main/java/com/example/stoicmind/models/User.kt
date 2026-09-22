package com.example.stoicmind.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val language: String = "en",
    val theme: String = "light",
    val primaryVirtue: String = "Wisdom",
    val preferences: List<String> = emptyList(),
    val points: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)