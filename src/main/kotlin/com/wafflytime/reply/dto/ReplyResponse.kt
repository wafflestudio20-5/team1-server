package com.wafflytime.reply.dto

import com.wafflytime.common.DateTimeResponse

data class ReplyResponse(
    val replyId: Long,
    val nickname: String,
    val createdAt: DateTimeResponse,
    val isRoot: Boolean,
    val contents: String,
    val isDeleted: Boolean,
    val isPostWriter: Boolean,
    val isMyReply: Boolean,
    val nLikes: Int,
)