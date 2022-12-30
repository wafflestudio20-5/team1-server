package com.wafflytime.database

import jakarta.persistence.Entity

@Entity
class RefreshTokenEntity(
    val userId: Long,
    var token: String,
): BaseTimeEntity()