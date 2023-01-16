package com.wafflytime.reply.dto

import com.wafflytime.notification.dto.NotificationRedirectInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.post.database.PostEntity


data class ReplyNotificationRedirectInfo (
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    override val notificationType: NotificationType = NotificationType.REPLY
) : NotificationRedirectInfo {
    constructor() : this(-1, "", -1)

    companion object {
        fun of(post: PostEntity) : ReplyNotificationRedirectInfo {
            return ReplyNotificationRedirectInfo(
                boardId = post.board.id,
                boardTitle = post.board.title,
                postId = post.id
            )
        }
    }
}
