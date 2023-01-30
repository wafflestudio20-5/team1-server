package com.wafflytime.user.auth.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.dto.OAuthResponse
import com.wafflytime.user.auth.dto.SocialSignUpRequest
import com.wafflytime.user.auth.service.OAuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
class OAuthController(
    private val oAuthService: OAuthService,
) {
    @ExemptAuthentication
    @PostMapping("/api/auth/social/login/{provider}")
    fun socialLogin(@PathVariable provider: String, @RequestParam code: String) : OAuthResponse {
        return oAuthService.socialLogin(provider, code)
    }

    @ExemptAuthentication
    @PostMapping("/api/auth/social/signup/{provider}")
    fun socialSignUp(@PathVariable provider: String, @RequestParam code: String, @Valid @RequestBody request: SocialSignUpRequest) : AuthToken {
        return oAuthService.socialSignUp(provider, code, request)
    }
}