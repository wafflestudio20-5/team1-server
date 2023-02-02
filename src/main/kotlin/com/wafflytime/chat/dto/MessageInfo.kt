package com.wafflytime.chat.dto

import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.common.DateTimeResponse
import java.time.LocalDateTime

data class MessageInfo(
    val id: Long,
    val sentAt: DateTimeResponse,
    val received: Boolean,
    val contents: String,
) {

    companion object {

        fun of(userId: Long, entity: MessageEntity): MessageInfo = entity.run {
            MessageInfo(
                id,
                DateTimeResponse.of(createdAt ?: LocalDateTime.now()),
                sender?.let { userId != it.id } ?: true,
                contents,
            )
        }
    }
}
