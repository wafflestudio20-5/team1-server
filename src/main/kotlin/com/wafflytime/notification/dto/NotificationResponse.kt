package com.wafflytime.notification.dto

import com.wafflytime.notification.database.NotificationEntity
import com.wafflytime.notification.type.NotificationType

data class NotificationResponse(
    val notificationType: NotificationType,
    val content: String,
    val redirectInfo: NotificationRedirectInfo? = null,
    val isRead: Boolean
) {
    companion object {
        fun of(notificationEntity: NotificationEntity) : NotificationResponse{
            return NotificationResponse(
                notificationType = notificationEntity.notificationType,
                content = notificationEntity.content,
                redirectInfo = notificationEntity.notificationRedirectInfo,
                isRead = notificationEntity.isRead
            )
        }
    }
}
