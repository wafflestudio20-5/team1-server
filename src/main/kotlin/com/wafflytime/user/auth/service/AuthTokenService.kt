package com.wafflytime.user.auth.service

import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.database.AuthTokenEntity
import com.wafflytime.user.auth.database.AuthTokenRepository
import com.wafflytime.user.auth.exception.AuthTokenExpired
import com.wafflytime.user.auth.exception.InvalidAuthToken
import com.wafflytime.user.auth.exception.AuthTokenTakenOver
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
import java.time.ZoneId

@ConfigurationProperties("auth.jwt")
data class AuthProperties @ConstructorBinding constructor(
    val issuer: String,
    val accessSecret: String,
    val refreshSecret: String,
    val expiration: Long,
    val refreshExpiration: Long,
)


interface AuthTokenService {
    fun createAuthToken(user: UserEntity, now: LocalDateTime): AuthToken
    fun updateAuthTokenEmailVerification(user: UserEntity, now: LocalDateTime): AuthToken
    fun refresh(refreshToken: String): AuthToken?
    fun deleteAuthTokenEntity(userId: Long, accessToken: String)
    fun authenticate(accessToken: String): Jws<Claims>
    fun getUserId(authResult: Jws<Claims>): Long
    fun isEmailVerified(authResult: Jws<Claims>): Boolean
    fun getExpiration(authResult: Jws<Claims>): LocalDateTime
}

@Service
@EnableConfigurationProperties(AuthProperties::class)
class AuthTokenServiceImpl(
    private val authProperties: AuthProperties,
    private val authTokenRepository: AuthTokenRepository,
) : AuthTokenService {
    private val tokenPrefix = "Bearer "
    private val accessSigningKey = Keys.hmacShaKeyFor(authProperties.accessSecret.toByteArray())
    private val refreshSigningKey = Keys.hmacShaKeyFor(authProperties.refreshSecret.toByteArray())

    override fun createAuthToken(user: UserEntity, now: LocalDateTime): AuthToken {
        authTokenRepository.findAllByUserId(user.id).filter {
            isExpired(it.refreshToken, refreshSigningKey)
        }.forEach {
            authTokenRepository.delete(it)
        }

        val authToken = buildAuthToken(user.id, now, user.univEmail != null)
        authToken.run {
            authTokenRepository.save(
                AuthTokenEntity(user.id, accessToken, refreshToken)
            )
        }
        return authToken
    }

    @Transactional
    override fun updateAuthTokenEmailVerification(user: UserEntity, now: LocalDateTime): AuthToken {
        authTokenRepository.deleteAllByUserId(user.id)
        return createAuthToken(user, now)
    }

    @Transactional
    override fun refresh(refreshToken: String): AuthToken? {
        val now = LocalDateTime.now()

        val authResult = verifyToken(refreshToken, refreshSigningKey)
        val userId = getUserId(authResult)
        val emailVerified = isEmailVerified(authResult)

        val authTokenEntity = try {
            findEntityByRefreshToken(refreshToken, userId)
        } catch (e: AuthTokenTakenOver) {
            return null
        }

        val authToken = buildAuthToken(userId, now, emailVerified)
        authTokenEntity.accessToken = authToken.accessToken
        authTokenEntity.refreshToken = authToken.refreshToken

        return authToken
    }

    @Transactional
    override fun deleteAuthTokenEntity(userId: Long, accessToken: String) {
        authTokenRepository.delete(findEntityByAccessToken(accessToken, userId))
    }

    override fun authenticate(accessToken: String): Jws<Claims> {
        return verifyToken(accessToken, accessSigningKey)
    }

    override fun getUserId(authResult: Jws<Claims>): Long {
        try {
            return authResult.body.subject.toLong()
        } catch (e: java.lang.NumberFormatException) {
            throw InvalidAuthToken
        }
    }

    override fun isEmailVerified(authResult: Jws<Claims>): Boolean {
        return authResult.body.get("email-verified", String::class.java)
            ?.toBoolean()
            ?: throw InvalidAuthToken
    }

    override fun getExpiration(authResult: Jws<Claims>): LocalDateTime {
        return authResult.body.expiration
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private fun findEntityByAccessToken(accessToken: String, userId: Long): AuthTokenEntity {
        val authTokenEntity =  authTokenRepository.findByAccessToken(parsePrefix(accessToken))
            ?: run { handleTokenTakeOver(listOf(userId)) }
        if (authTokenEntity.userId != userId) {
            handleTokenTakeOver(listOf(userId, authTokenEntity.userId))
        }

        return authTokenEntity
    }

    private fun findEntityByRefreshToken(refreshToken: String, userId: Long): AuthTokenEntity {
        val authTokenEntity = authTokenRepository.findByRefreshToken(parsePrefix(refreshToken))
            ?: run { handleTokenTakeOver(listOf(userId)) }
        if (authTokenEntity.userId != userId) {
            handleTokenTakeOver(listOf(userId, authTokenEntity.userId))
        }

        return authTokenEntity
    }

    private fun buildAuthToken(userId: Long, now: LocalDateTime, emailVerified: Boolean): AuthToken {
        val claims = Jwts.claims()
        claims["email-verified"] = if (emailVerified) "true" else "false"

        val accessToken = buildJwtToken(
            authProperties.issuer,
            userId.toString(),
            accessSigningKey,
            now,
            now.plusMinutes(authProperties.expiration),
            claims
        )
        val refreshToken = buildJwtToken(
            authProperties.issuer,
            userId.toString(),
            refreshSigningKey,
            now,
            now.plusDays(authProperties.refreshExpiration),
            claims
        )

        return AuthToken(
            accessToken,
            refreshToken,
        )
    }

    private fun buildJwtToken(
        issuer: String,
        subject: String,
        key: Key,
        issuedAt: LocalDateTime,
        expiration: LocalDateTime,
        claims: Claims
    ): String {
        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(subject)
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(key, SignatureAlgorithm.HS512)
            .addClaims(claims)
            .compact()
    }

    private fun handleTokenTakeOver(userId: List<Long>): Nothing {
        userId.forEach {
            authTokenRepository.deleteAllByUserId(it)
        }
        throw AuthTokenTakenOver
    }

    private fun verifyToken(token: String, key: Key): Jws<Claims> {
        try {
            return parse(token, key)
        } catch (e: ExpiredJwtException) {
            throw AuthTokenExpired
        } catch (e: Exception) {
            throw InvalidAuthToken
        }
    }

    private fun isExpired(token: String, key: Key): Boolean {
        try {
            parse(token, key)
        } catch (e: Exception) {
            return true
        }

        return false
    }

    private fun parsePrefix(token: String): String {
        return token.replace(tokenPrefix, "").trim { it <= ' ' }
    }

    private fun parse(token: String, key: Key): Jws<Claims> {
        val prefixRemoved = parsePrefix(token)
        return Jwts.parserBuilder()
            .requireIssuer(authProperties.issuer)
            .setSigningKey(key)
            .build()
            .parseClaimsJws(prefixRemoved)
    }
}