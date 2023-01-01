package com.wafflytime.user.auth.controller.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.user.auth.controller.dto.AuthToken
import com.wafflytime.user.auth.service.AuthTokenService
import com.wafflytime.user.auth.controller.dto.LoginRequest
import com.wafflytime.user.auth.controller.dto.SignUpRequest
import com.wafflytime.user.auth.service.LocalAuthService
import jakarta.validation.Valid
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

    @ExemptAuthentication
    @PostMapping("/api/auth/local/login")
    fun localLogin(@Valid @RequestBody request: LoginRequest) : AuthToken {
        return localAuthService.login(request)
    }

    @ExemptEmailVerification
    @DeleteMapping("/api/auth/logout")
    fun logout(@UserIdFromToken userId: Long) : String {
        authTokenService.deleteRefreshToken(userId)
        return "success"
    }

    @ExemptAuthentication
    @PutMapping("/api/auth/refresh")
    fun refresh(@RequestHeader(name = "Authorization") refreshToken: String) : AuthToken {
        return authTokenService.refresh(refreshToken)
    }

}