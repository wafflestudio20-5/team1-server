package com.wafflytime.chat.dto

import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.common.DateTimeResponse

data class MessageInfo(
    val sentAt: DateTimeResponse,
    val received: Boolean,
    val content: String,
) {

    companion object {
        fun of(userId: Long, entity: MessageEntity): MessageInfo = entity.run {
            MessageInfo(
                DateTimeResponse.of(createdAt!!),
                sender?.let { userId != it.id } ?: true,
                content,
            )
        }
    }
}
