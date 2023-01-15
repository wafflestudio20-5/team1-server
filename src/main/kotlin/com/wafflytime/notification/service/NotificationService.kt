package com.wafflytime.notification.service

import com.wafflytime.notification.database.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

}