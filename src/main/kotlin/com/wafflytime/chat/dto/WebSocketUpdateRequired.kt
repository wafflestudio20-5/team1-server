package com.wafflytime.chat.dto

data class WebSocketUpdateRequired(
    val type: WebSocketJsonType = WebSocketJsonType.NEED_UPDATE
)
