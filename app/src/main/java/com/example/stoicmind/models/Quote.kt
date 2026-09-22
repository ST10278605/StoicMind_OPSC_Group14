package com.example.stoicmind.models

data class Quote(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val category: String = "",
    val explanation: String = "",
    val reflectionQuestion: String = "",
    val date: String = "",
    val isFavorite: Boolean = false
)