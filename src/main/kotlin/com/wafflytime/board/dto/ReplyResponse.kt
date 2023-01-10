package com.wafflytime.board.dto

data class ReplyResponse(
    val replyId: Long,
    val writer: ReplyWriterResponse,
    val parent: ReplyWriterResponse?,
    val mention: ReplyWriterResponse?,
    val contents: String,
    val isDeleted: Boolean,
)

data class ReplyWriterResponse(
    val writerId: Long,
    val anonymousId: Long,
    val isWriterAnonymous: Boolean = true,
)