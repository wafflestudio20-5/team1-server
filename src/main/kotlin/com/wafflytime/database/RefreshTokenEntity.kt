package com.wafflytime.database

import jakarta.persistence.Entity

@Entity
class RefreshTokenEntity(
    val userId: Long,
    var token: String?,
    // TODO: ip or 접속 기기?
): BaseTimeEntity()