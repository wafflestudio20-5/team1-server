package com.wafflytime.chat.dto

data class CreateChatResponse(
    val new: Boolean,
    val chatInfo: ChatSimpleInfo,
    val systemMessageInfo: MessageInfo?,
    val firstMessageInfo: MessageInfo,
)
