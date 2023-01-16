package com.wafflytime.notification.dto

import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity

interface NotificationInfo {
    val notificationType: NotificationType
}

data class NotificationDto (
    val receiver: UserEntity,
    val content: String,
    val notificationInfo: NotificationInfo
)