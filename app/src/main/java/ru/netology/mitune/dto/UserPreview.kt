package ru.netology.mitune.dto

data class UserPreview(
    val id: Int = 0,
    val name: String,
    val avatarUrl: String? = null,
)