package ru.netology.mitune.model

data class FeedModelState(
    val loading: Boolean = false,
    val refreshing: Boolean = true,
    val error: Boolean = false,
)