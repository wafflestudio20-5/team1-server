package com.wafflytime.reply.dto

import com.wafflytime.notification.dto.NotificationInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.post.database.PostEntity


data class ReplyNotificationInfo (
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    override val notificationType: NotificationType = NotificationType.REPLY
) : NotificationInfo {
    constructor() : this(-1, "", -1)

    companion object {
        fun of(post: PostEntity) : ReplyNotificationInfo {
            return ReplyNotificationInfo(
                boardId = post.board.id,
                boardTitle = post.board.title,
                postId = post.id
            )
        }
    }
}
