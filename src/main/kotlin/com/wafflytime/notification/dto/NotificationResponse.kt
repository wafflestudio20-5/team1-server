package com.wafflytime.notification.dto

import com.wafflytime.common.DateTimeResponse
import com.wafflytime.notification.database.NotificationEntity
import com.wafflytime.notification.type.NotificationType

data class NotificationResponse(
    val notificationId: Long,
    val notificationType: NotificationType,
    val content: String,
    val contentCreatedAt: DateTimeResponse,
    val info: NotificationInfo? = null,
    val isRead: Boolean
) {

    companion object {
        fun of(notificationEntity: NotificationEntity) : NotificationResponse{
            return NotificationResponse(
                notificationId = notificationEntity.id,
                notificationType = notificationEntity.notificationType,
                content = notificationEntity.content,
                contentCreatedAt = DateTimeResponse.of(notificationEntity.contentCreatedAt!!),
                info = notificationEntity.info,
                isRead = notificationEntity.isRead
            )
        }
    }
}
