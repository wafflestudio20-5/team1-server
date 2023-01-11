package com.wafflytime.reply.dto

import jakarta.validation.constraints.NotEmpty

data class CreateReplyRequest(
    @NotEmpty
    val contents: String,
    val mention: Long? = null,
    val isWriterAnonymous: Boolean = true,
)