package com.wafflytime.chat.dto

data class CreateChatRequest(
    val isAnonymous: Boolean,
    val content: String,
)
