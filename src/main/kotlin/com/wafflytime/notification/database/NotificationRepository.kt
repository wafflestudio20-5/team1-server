package com.wafflytime.notification.database

import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<NotificationEntity, Long> {
}