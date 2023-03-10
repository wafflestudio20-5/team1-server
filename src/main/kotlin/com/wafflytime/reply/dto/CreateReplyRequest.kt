package com.wafflytime.reply.dto

import jakarta.validation.constraints.NotEmpty

data class CreateReplyRequest(
    @NotEmpty
    val contents: String,
    val parent: Long? = null,
    val isWriterAnonymous: Boolean = true,
)