package ru.netology.mitune.dto

data class User(
    val id : Int = 0,
    val login : String  = "",
    val name : String = "",
    val avatar : String? = null,
    val isChecked : Boolean = false
)
