package com.wafflytime.user.auth.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name="auth_token")
class AuthTokenEntity(
    val userId: Long,
    @field:Column(length = 255)
    var accessToken: String,
    @field:Column(length = 255)
    var refreshToken: String,
): BaseTimeEntity()