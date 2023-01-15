package com.wafflytime.notification.dto

import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity

interface NotificationRedirectInfo {}

interface NotificationDto {
    val receiver: UserEntity
    val content: String
    val notificationType: NotificationType
    val notificationRedirectInfo: NotificationRedirectInfo
}