package com.wafflytime.chat.dto

data class WebSocketClientMessage(
    val chatId: Long,
    val contents: String,
) {
    constructor() : this(0, "")
}
