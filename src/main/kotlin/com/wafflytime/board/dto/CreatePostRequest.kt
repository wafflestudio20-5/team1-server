package com.wafflytime.board.dto

import jakarta.validation.constraints.NotEmpty

data class CreatePostRequest(
    val title: String? = null,
    @NotEmpty
    val contents: String,
    val isQuestion: Boolean = false,
    val isWriterAnonymous: Boolean = true,
    val images: List<ImageRequest>? = null
)