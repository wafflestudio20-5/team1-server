package com.wafflytime.user.auth.controller.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.auth.controller.dto.AuthToken
import com.wafflytime.user.auth.controller.dto.SocialLoginRequest
import com.wafflytime.user.auth.controller.dto.SocialSignUpRequest
import com.wafflytime.user.auth.service.OAuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
class OAuthController(
    private val oAuthService: OAuthService,
) {

    @ExemptAuthentication
    @RequestMapping("/index")
    fun index() = "index"

    @ExemptAuthentication
    @PostMapping("/api/auth/social/login/{provider}")
    fun getSocialEmail(@PathVariable provider: String, @RequestParam code: String) : String {
        return oAuthService.getSocialEmail(provider, code)
    }

    @ExemptAuthentication
    @PostMapping("/api/auth/social/login")
    fun socialLogin(@Valid @RequestBody request: SocialLoginRequest) : AuthToken {
        return oAuthService.socialLogin(request)
    }

    @ExemptAuthentication
    @PostMapping("/api/auth/social/signup")
    fun localSignUp(@Valid @RequestBody request: SocialSignUpRequest) : AuthToken {
        return oAuthService.socialSignUp(request)
    }
}