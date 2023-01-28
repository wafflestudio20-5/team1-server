package com.wafflytime.chat.dto

import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.common.DateTimeResponse
import java.time.LocalDateTime

data class WebSocketReceiveMessage(
    val chatId: Long,
    val sentAt: DateTimeResponse,
    val received: Boolean,
    val contents: String,
) {

    companion object {

        fun of(userId: Long, entity: MessageEntity): WebSocketReceiveMessage = entity.run {
            WebSocketReceiveMessage(
                chat.id,
                DateTimeResponse.of(createdAt ?: LocalDateTime.now()),
                sender?.let { userId != it.id } ?: true,
                contents,
            )
        }

        fun senderAndReceiverPair(entity: MessageEntity): Pair<WebSocketReceiveMessage, WebSocketReceiveMessage> = entity.run {
            val chatId = chat.id
            val sentAt = DateTimeResponse.of(createdAt!!)
            val contents = contents

            Pair(
                WebSocketReceiveMessage(chatId, sentAt, false, contents),
                WebSocketReceiveMessage(chatId, sentAt, true, contents),
            )
        }
    }
}
