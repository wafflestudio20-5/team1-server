package com.wafflytime.user.auth.service

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.auth.api.dto.AuthToken
import com.wafflytime.user.auth.api.dto.LoginRequest
import com.wafflytime.user.auth.api.dto.SignUpRequest
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.user.auth.controller.dto.TempAdminSignUpRequest
import jakarta.transaction.Transactional
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
        // TODO 중복된 loginId가 없는지 exception 처리해줘야 함
        val user = userRepository.save(
            UserEntity(
                request.id,
                passwordEncoder.encode(request.password)
            )
        )
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    @ExemptAuthentication
    @Transactional
    override fun adminSignUp(request: TempAdminSignUpRequest): AuthToken {
        val user = userRepository.save(
            UserEntity(
                loginId = request.id,
                password = passwordEncoder.encode(request.password),
                univEmail = request.univEmail,
                isAdmin = true
            )
        )
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }


    @ExemptAuthentication
    @Transactional
    override fun login(request: LoginRequest): AuthToken {
        val user = userRepository.findByLoginId(request.id) ?: throw WafflyTime404("존재하지 않는 아이디이거나 비밀번호가 잘못되었습니다")

        if (passwordEncoder.matches(request.password, user.password)) {
            return authTokenService.buildAuthToken(user, LocalDateTime.now())
        } else {
            throw WafflyTime404("존재하지 않는 아이디이거나 비밀번호가 잘못되었습니다")
        }
    }

}