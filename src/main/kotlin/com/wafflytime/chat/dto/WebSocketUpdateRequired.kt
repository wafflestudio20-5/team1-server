package com.wafflytime.chat.dto

data class WebSocketUpdateRequired(
    val chatId: List<Long>,
    val unread: List<Int>,
    val type: WebSocketJsonType = WebSocketJsonType.NEED_UPDATE
)
