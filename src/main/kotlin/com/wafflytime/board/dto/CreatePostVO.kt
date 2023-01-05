package com.wafflytime.board.dto

import jakarta.validation.constraints.NotEmpty
import org.springframework.web.multipart.MultipartFile

data class CreatePostVO(
    val title: String? = null,
    @NotEmpty
    val contents: String,
    val isQuestion: Boolean = false,
    val isWriterAnonymous: Boolean = true,
    val files: List<MultipartFile>?
)