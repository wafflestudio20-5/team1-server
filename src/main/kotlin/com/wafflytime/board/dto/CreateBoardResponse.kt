package com.wafflytime.board.dto

data class CreateBoardResponse(
    val userId: Long,
    val boardId: Long,
    val title: String,
    val description: String,
    val allowAnonymous: Boolean
)
