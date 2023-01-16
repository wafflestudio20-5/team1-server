package com.wafflytime.chat.dto

import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.exception.UserChatMismatch

data class ChatSimpleInfo(
    val id: Long,
    val target: String,
    val recentMessage: String,
    val unread: Int,
) {

    companion object {

        private const val anonymousName = "익명"

        fun of(userId: Long, entity: ChatEntity): ChatSimpleInfo = entity.run {
            val recentMessage = messages.last()

            when (userId) {
                participant1.id -> ChatSimpleInfo(
                    id,
                    if (isAnonymous1) anonymousName else participant1.nickname,
                    recentMessage.content,
                    unread1,
                )
                participant2.id -> ChatSimpleInfo(
                    id,
                    if (isAnonymous2) anonymousName else participant2.nickname,
                    recentMessage.content,
                    unread2,
                )
                else -> throw UserChatMismatch
            }
        }

    }
}