package com.wafflytime.reply.dto

data class ReplyResponse(
    val replyId: Long,
    val writerId: Long,
    val nickname: String,
    val mention: String?,
    val contents: String,
    val isDeleted: Boolean,
    val isPostWriter: Boolean,
)