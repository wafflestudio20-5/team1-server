package com.wafflytime.board.dto

import jakarta.validation.constraints.NotEmpty

data class CreatePostRequest(
    @NotEmpty
    val title: String,
    @NotEmpty
    val contents: String,
    val isQuestion: Boolean = false,
    val isWriterAnonymous: Boolean = true
)