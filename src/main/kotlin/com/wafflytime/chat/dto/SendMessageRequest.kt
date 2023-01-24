package com.wafflytime.chat.dto

import jakarta.validation.constraints.NotBlank

data class SendMessageRequest(
    @NotBlank
    val content: String,
)