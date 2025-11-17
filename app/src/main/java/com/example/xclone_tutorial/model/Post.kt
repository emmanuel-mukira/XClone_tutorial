package com.example.xclone_tutorial.model

data class Post(
    val id: String = "",
    val authorName: String = "",
    val handle: String = "",
    val text: String = "",
    val likeCount: Int = 0,
    val timestamp: Long = 0L
)
