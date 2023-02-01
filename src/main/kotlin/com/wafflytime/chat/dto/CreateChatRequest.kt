package com.wafflytime.chat.dto

import jakarta.validation.constraints.NotBlank

data class CreateChatRequest(
    val isAnonymous: Boolean,
    @field:NotBlank
    val contents: String,
)
