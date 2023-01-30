package com.wafflytime.chat.dto

import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.common.DateTimeResponse
import java.time.LocalDateTime

data class WebSocketServerMessage(
    val chatId: Long,
    val sentAt: DateTimeResponse,
    val received: Boolean,
    val contents: String,
    val type: WebSocketJsonType = WebSocketJsonType.MESSAGE,
) {

    companion object {

        fun of(userId: Long, entity: MessageEntity): WebSocketServerMessage = entity.run {
            WebSocketServerMessage(
                chat.id,
                DateTimeResponse.of(createdAt ?: LocalDateTime.now()),
                sender?.let { userId != it.id } ?: true,
                contents,
            )
        }

        fun senderAndReceiverPair(entity: MessageEntity): Pair<WebSocketServerMessage, WebSocketServerMessage> = entity.run {
            val chatId = chat.id
            val sentAt = DateTimeResponse.of(createdAt!!)
            val contents = contents

            Pair(
                WebSocketServerMessage(chatId, sentAt, false, contents),
                WebSocketServerMessage(chatId, sentAt, true, contents),
            )
        }
    }
}
