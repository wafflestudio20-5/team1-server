package com.wafflytime.reply.dto

import com.wafflytime.notification.dto.NotificationDto
import com.wafflytime.notification.dto.NotificationRedirectInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.post.database.PostEntity
import com.wafflytime.user.info.database.UserEntity


data class ReplyNotificationRedirectInfo (
    val boardId: Long,
    val boardTitle: String,
    val postId: Long
) : NotificationRedirectInfo {}


data class ReplyNotificationDto (
    override val receiver: UserEntity,
    override val content: String,
    override val notificationType: NotificationType = NotificationType.REPLY,
    override val notificationRedirectInfo : NotificationRedirectInfo
) : NotificationDto {
    companion object {
        fun of(receiver: UserEntity, content: String, post:PostEntity) : ReplyNotificationDto {
            return ReplyNotificationDto(
                receiver=receiver,
                content = content,
                notificationRedirectInfo = ReplyNotificationRedirectInfo(
                    boardId=post.board.id, boardTitle = post.board.title, postId = post.id
                )
            )
        }
    }
}

