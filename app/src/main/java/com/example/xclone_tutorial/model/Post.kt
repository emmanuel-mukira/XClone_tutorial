package com.example.xclone_tutorial.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val handle: String = "",
    val text: String = "",
    val likeCount: Int = 0,
    val timestamp: Long = 0L
)

// Used for UI to track if current user liked this post
data class PostWithLikeState(
    val post: Post,
    val isLikedByCurrentUser: Boolean = false
)
