package com.wafflytime.board.dto

data class UpdateReplyRequest(
    val contents: String?,
    val isWriterAnonymous: Boolean?,
)