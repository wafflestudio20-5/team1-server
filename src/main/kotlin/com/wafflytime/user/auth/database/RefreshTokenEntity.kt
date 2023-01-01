package com.wafflytime.user.auth.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class RefreshTokenEntity(
    val userId: Long,
    @field:Column(length = 255)
    var token: String?,
    // TODO: ip or 접속 기기?
): BaseTimeEntity()