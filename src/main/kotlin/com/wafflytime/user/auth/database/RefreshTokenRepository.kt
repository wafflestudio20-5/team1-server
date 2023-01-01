package com.wafflytime.user.auth.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByUserId(userId: Long): RefreshTokenEntity?
}