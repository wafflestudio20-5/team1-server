package com.wafflytime.notification.database

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<NotificationEntity, Long> {
    fun findAllByReceiverId(userId: Long, pageable: Pageable) : Page<NotificationEntity>
}