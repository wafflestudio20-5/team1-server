package com.wafflytime.reply.dto

data class UpdateReplyRequest(
    val contents: String?,
    val isWriterAnonymous: Boolean?,
)