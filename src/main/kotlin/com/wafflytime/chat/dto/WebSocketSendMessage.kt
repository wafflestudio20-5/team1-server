package com.wafflytime.chat.dto

data class WebSocketSendMessage(
    val chatId: Long,
    val contents: String,
) {
    constructor() : this(0, "")
}
