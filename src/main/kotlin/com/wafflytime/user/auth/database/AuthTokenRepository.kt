package com.wafflytime.user.auth.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthTokenRepository : JpaRepository<AuthTokenEntity, Long> {
    fun deleteAllByUserId(userId: Long)
    fun findAllByUserId(userId: Long): List<AuthTokenEntity>
    fun findByAccessToken(accessToken: String): AuthTokenEntity?
    fun findByRefreshToken(refreshToken: String): AuthTokenEntity?
}