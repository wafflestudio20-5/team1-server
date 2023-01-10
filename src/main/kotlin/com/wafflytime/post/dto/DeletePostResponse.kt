package com.wafflytime.post.dto


data class DeletePostResponse(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    val postTitle: String?
)
