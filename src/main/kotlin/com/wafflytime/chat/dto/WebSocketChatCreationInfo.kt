package com.wafflytime.chat.dto

import com.wafflytime.chat.database.ChatEntity

data class WebSocketChatCreationInfo(
    val chatId: Long,
    val target: String,
    val type: WebSocketJsonType = WebSocketJsonType.NEWCHAT,
) {

    companion object {

        private const val anonymousName = "익명"

        fun senderAndReceiverPair(entity: ChatEntity): Pair<WebSocketChatCreationInfo, WebSocketChatCreationInfo> = entity.run {
            Pair(
                WebSocketChatCreationInfo(id, if(isAnonymous2) anonymousName else participant2.nickname),
                WebSocketChatCreationInfo(id, if(isAnonymous1) anonymousName else participant1.nickname),
            )
        }

    }
}