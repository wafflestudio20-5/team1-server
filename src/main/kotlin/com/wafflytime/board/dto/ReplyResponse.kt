package com.wafflytime.board.dto

data class ReplyResponse(
    val replyId: Long,
    val writer: ReplyWriterResponse,
    val parent: ReplyWriterResponse? = null,
    val mention: ReplyWriterResponse? = null,
    val contents: String,
)

data class ReplyWriterResponse(
    val writerId: Long,
    val anonymousId: Long,
    val isWriterAnonymous: Boolean = true,
)