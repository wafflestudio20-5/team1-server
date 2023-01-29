package com.wafflytime.user.mail.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MailVerificationRepository : JpaRepository<MailVerificationEntity, Long> {
    fun findByUserId(userId: Long): MailVerificationEntity?
}