package com.wafflytime.user.auth.service

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.dto.LoginRequest
import com.wafflytime.user.auth.dto.SignUpRequest
import com.wafflytime.user.auth.dto.TempAdminSignUpRequest
import com.wafflytime.user.auth.exception.LoginFailure
import com.wafflytime.user.auth.exception.SignUpConflict
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface LocalAuthService {
    fun signUp(request: SignUpRequest): AuthToken
    fun login(request: LoginRequest): AuthToken
    fun adminSignUp(request: TempAdminSignUpRequest): AuthToken
}

@Service
class LocalAuthServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
): LocalAuthService {

    @ExemptAuthentication
    @Transactional
    override fun signUp(request: SignUpRequest): AuthToken {
        val user = try {
            userRepository.save(
                UserEntity(
                    loginId = request.id,
                    password = passwordEncoder.encode(request.password),
                    nickname = request.nickname,
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw SignUpConflict
        }
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    @ExemptAuthentication
    @Transactional
    override fun adminSignUp(request: TempAdminSignUpRequest): AuthToken {

        val user = try {
            userRepository.save(
                UserEntity(
                    loginId = request.id,
                    password = passwordEncoder.encode(request.password),
                    univEmail = request.univEmail,
                    nickname = request.nickname,
                    isAdmin = true
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw SignUpConflict
        }
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }


    @ExemptAuthentication
    @Transactional
    override fun login(request: LoginRequest): AuthToken {
        val user = userRepository.findByLoginId(request.id) ?: throw LoginFailure

        if (passwordEncoder.matches(request.password, user.password)) {
            return authTokenService.buildAuthToken(user, LocalDateTime.now())
        } else {
            throw LoginFailure
        }
    }

}