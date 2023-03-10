package com.wafflytime.user.auth.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.service.AuthTokenService
import com.wafflytime.user.auth.dto.LoginRequest
import com.wafflytime.user.auth.dto.SignUpRequest

import com.wafflytime.user.auth.dto.TempAdminSignUpRequest
import com.wafflytime.user.auth.exception.AuthTokenTakenOver
import com.wafflytime.user.auth.service.LocalAuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val localAuthService: LocalAuthService,
    private val authTokenService: AuthTokenService,
) {

    @ExemptAuthentication
    @PostMapping("/api/auth/local/signup")
    fun localSignUp(@Valid @RequestBody request: SignUpRequest) : AuthToken {
        return localAuthService.signUp(request)
    }


    // TODO(재웅) : 서버 관리자만 이 api를 호출할 수 있게 변경해야 한다
    @ExemptAuthentication
    @PostMapping("/api/auth/admin/signup")
    fun adminSignUp(@Valid @RequestBody request: TempAdminSignUpRequest) : AuthToken {
        return localAuthService.adminSignUp(request)
    }

    @ExemptAuthentication
    @PostMapping("/api/auth/local/login")
    fun localLogin(@Valid @RequestBody request: LoginRequest) : AuthToken {
        return localAuthService.login(request)
    }

    @ExemptEmailVerification
    @DeleteMapping("/api/auth/logout")
    fun logout(
        @UserIdFromToken userId: Long,
        @RequestHeader(required = true, name = "Authorization") accessToken: String
    ) : String {
        authTokenService.deleteAuthTokenEntity(userId, accessToken)
        return "success"
    }

    @ExemptAuthentication
    @PutMapping("/api/auth/refresh")
    fun refresh(@RequestHeader(required = true, name = "Authorization") refreshToken: String) : ResponseEntity<Any> {
        return authTokenService.refresh(refreshToken)?.let { ResponseEntity.ok(it) }
            ?: AuthTokenTakenOver.toResponse()
    }

}