package com.wafflytime.notification.dto

import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.post.database.PostEntity
import com.wafflytime.reply.database.ReplyEntity
import com.wafflytime.user.info.database.UserEntity
import java.time.LocalDateTime


data class NotificationInfo (
    val boardId: Long? = null,
    val boardTitle: String? = null,
    val postId: Long? = null,
    val chatId: Long? = null
) {
    constructor() : this(null, null, null, null)

    companion object {

        fun fromReply(post: PostEntity) : NotificationInfo {
            return NotificationInfo(
                boardId = post.board.id,
                boardTitle = post.board.title,
                postId = post.id
            )
        }

        fun fromMessage(message: MessageEntity) : NotificationInfo {
            return NotificationInfo(
                chatId = message.chat.id
            )
        }

    }
}

data class NotificationDto (
    val notificationType: NotificationType,
    val receiver: UserEntity,
    val content: String,
    val contentCreatedAt: LocalDateTime?,
    val notificationInfo: NotificationInfo
) {

    companion object {

        fun fromReply(receiver: UserEntity, reply: ReplyEntity) : NotificationDto {
            return NotificationDto(
                notificationType = NotificationType.REPLY,
                receiver=receiver,
                content = NotificationType.REPLY.prefix + reply.contents,
                contentCreatedAt = reply.createdAt,
                notificationInfo = NotificationInfo.fromReply(reply.post)
            )
        }

        fun fromMessage(receiver: UserEntity, message: MessageEntity) : NotificationDto {
            return NotificationDto(
                notificationType = NotificationType.MESSAGE,
                receiver = receiver,
                content = NotificationType.MESSAGE.prefix + message.contents,
                contentCreatedAt = message.createdAt,
                notificationInfo = NotificationInfo.fromMessage(message)
            )
        }

    }
}