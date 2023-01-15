package com.wafflytime.user.auth.service

import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.database.RefreshTokenEntity
import com.wafflytime.user.auth.database.RefreshTokenRepository
import com.wafflytime.user.auth.exception.AuthTokenExpired
import com.wafflytime.user.auth.exception.InvalidAuthToken
import com.wafflytime.user.auth.exception.RefreshTokenTakenOver
import com.wafflytime.user.info.database.UserEntity
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.transaction.Transactional
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.stereotype.Service
import java.security.Key
import java.sql.Timestamp
import java.time.LocalDateTime

@ConfigurationProperties("auth.jwt")
data class AuthProperties @ConstructorBinding constructor(
    val issuer: String,
    val accessSecret: String,
    val refreshSecret: String,
    val expiration: Long,
    val refreshExpiration: Long,
)


interface AuthTokenService {
    fun buildAuthToken(user: UserEntity, now: LocalDateTime): AuthToken
    fun refresh(refreshToken: String): AuthToken
    fun deleteRefreshToken(userId: Long)
    fun authenticate(accessToken: String): Jws<Claims>
    fun getUserId(authResult: Jws<Claims>): Long
    fun isEmailVerified(authResult: Jws<Claims>): Boolean
}

@Service
@EnableConfigurationProperties(AuthProperties::class)
class AuthTokenServiceImpl(
    private val authProperties: AuthProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
): AuthTokenService {
    private val tokenPrefix = "Bearer "
    private val accessSigningKey = Keys.hmacShaKeyFor(authProperties.accessSecret.toByteArray())
    private val refreshSigningKey = Keys.hmacShaKeyFor(authProperties.refreshSecret.toByteArray())

    override fun buildAuthToken(user: UserEntity, now: LocalDateTime): AuthToken {
        return buildAuthToken(user.id, now, user.univEmail != null)
    }

    // TODO: 1대다가 되는 경우 추가 구현 필요
    @Transactional
    override fun refresh(refreshToken: String): AuthToken {
        val now = LocalDateTime.now()

        val authResult = verifyToken(refreshToken, refreshSigningKey)
        val userId = getUserId(authResult)
        val emailVerified = isEmailVerified(authResult)

        val refreshTokenEntity = refreshTokenRepository.findByUserId(userId)
            ?: throw InvalidAuthToken

        if (refreshToken == tokenPrefix + refreshTokenEntity.token) {
            return buildAuthToken(userId, now, emailVerified)
        } else {
            throw RefreshTokenTakenOver
        }
    }

    @Transactional
    override fun deleteRefreshToken(userId: Long) {
        val refreshTokenEntity = refreshTokenRepository.findByUserId(userId)
            ?: throw InvalidAuthToken

        refreshTokenEntity.token = null
    }

    override fun authenticate(accessToken: String): Jws<Claims> {
        return verifyToken(accessToken, accessSigningKey)
    }

    override fun getUserId(authResult: Jws<Claims>): Long {
        try {
            return authResult.body.subject.toLong()
        } catch(e: java.lang.NumberFormatException) {
            throw InvalidAuthToken
        }
    }

    override fun isEmailVerified(authResult: Jws<Claims>): Boolean {
        return authResult.body.get("email-verified", String::class.java)
            ?.toBoolean()
            ?: throw InvalidAuthToken
    }

    private fun buildAuthToken(userId: Long, now: LocalDateTime, emailVerified: Boolean): AuthToken {
        val claims = Jwts.claims()
        claims["email-verified"] = if (emailVerified) "true" else "false"

        val accessToken = buildJwtToken(authProperties.issuer, userId.toString(), accessSigningKey, now, now.plusMinutes(authProperties.expiration), claims)
        val refreshToken = buildJwtToken(authProperties.issuer, userId.toString(), refreshSigningKey, now, now.plusDays(authProperties.refreshExpiration), claims)

        refreshTokenRepository.save(
            refreshTokenRepository.findByUserId(userId)?.apply {
                token = refreshToken
            } ?: RefreshTokenEntity(
                userId,
                refreshToken,
            )
        )

        return AuthToken(
            accessToken,
            refreshToken,
        )
    }

    private fun buildJwtToken(issuer: String, subject: String, key: Key, issuedAt: LocalDateTime, expiration: LocalDateTime, claims: Claims): String {
        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(subject)
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(key, SignatureAlgorithm.HS512)
            .addClaims(claims)
            .compact()
    }

    private fun verifyToken(token: String, key: Key): Jws<Claims> {
        try {
            return parse(token, key)
        } catch (ex: ExpiredJwtException) {
            throw AuthTokenExpired
        } catch (ex: Exception) {
            throw InvalidAuthToken
        }
    }

    private fun parse(token: String, key: Key): Jws<Claims> {
        val prefixRemoved = token.replace(tokenPrefix, "").trim { it <= ' ' }
        return Jwts.parserBuilder()
            .requireIssuer(authProperties.issuer)
            .setSigningKey(key)
            .build()
            .parseClaimsJws(prefixRemoved)
    }
}