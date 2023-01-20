package com.wafflytime.user.mail.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "mail_verification")
class MailVerificationEntity(
    @field:Column(unique = true)
    val userId: Long,
    val code: String,
    val email: String,
): BaseTimeEntity()