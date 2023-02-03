package com.wafflytime.chat.dto

import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.exception.UserChatMismatch
import com.wafflytime.common.DateTimeResponse

data class ChatSimpleInfo(
    val id: Long,
    val target: String,
    val recentMessage: String,
    val recentTime: DateTimeResponse,
    val unread: Int,
    val blocked: Boolean,
) {

    companion object {

        private const val anonymousName = "익명"

        fun of(userId: Long, entity: ChatEntity): ChatSimpleInfo = entity.run {
            val recentMessage = messages.first()

            when (userId) {
                participant1.id -> ChatSimpleInfo(
                    id,
                    if (isAnonymous2) anonymousName else participant2.nickname,
                    recentMessage.contents,
                    DateTimeResponse.of(recentMessage.createdAt!!),
                    unread1,
                    blocked1,
                )
                participant2.id -> ChatSimpleInfo(
                    id,
                    if (isAnonymous1) anonymousName else participant1.nickname,
                    recentMessage.contents,
                    DateTimeResponse.of(recentMessage.createdAt!!),
                    unread2,
                    blocked2,
                )
                else -> throw UserChatMismatch
            }
        }

    }
}