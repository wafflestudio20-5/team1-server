package com.wafflytime.chat.dto

import com.wafflytime.chat.database.ChatEntity

data class WebSocketChatCreationInfo(
    val chatId: Long,
    val target: String,
    val type: WebSocketJsonType = WebSocketJsonType.NEWCHAT,
) {

    companion object {

        private const val anonymousName = "익명"

        fun of(entity: ChatEntity): WebSocketChatCreationInfo = entity.run {
            WebSocketChatCreationInfo(
                id,
                if (isAnonymous2) anonymousName else participant2.nickname,
            )
        }
    }
}