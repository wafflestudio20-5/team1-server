package com.wafflytime.chat.dto

data class UpdateUnreadRequest(
    val chatId: List<Long>,
    val unread: List<Int>,
)
