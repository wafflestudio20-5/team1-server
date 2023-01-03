package com.wafflytime.board.dto

data class UpdatePostRequest(
    val title: String?,
    val contents: String?,
    val isQuestion: Boolean?,
    val isWriterAnonymous: Boolean?
)
