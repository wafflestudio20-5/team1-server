package com.wafflytime.user.auth.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name="refresh_token")
class RefreshTokenEntity(
    val userId: Long,
    @field:Column(length = 255)
    var token: String?,
    // TODO: ip or 접속 기기?
): BaseTimeEntity()