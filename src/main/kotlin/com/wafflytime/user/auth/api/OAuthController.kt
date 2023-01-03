package com.wafflytime.user.auth.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.auth.api.dto.AuthToken
import com.wafflytime.user.auth.service.OAuthService
import org.springframework.web.bind.annotation.*

@RestController
class OAuthController(
    private val oAuthService: OAuthService,
) {
    @ExemptAuthentication
    @PostMapping("/api/auth/social/login/{provider}")
    fun socialLogin(@PathVariable provider: String, @RequestParam code: String) : AuthToken {
        return oAuthService.socialLogin(provider, code)
    }

    @ExemptAuthentication
    @PostMapping("/api/auth/social/signup/{provider}")
    fun socialSignUp(@PathVariable provider: String, @RequestParam code: String) : AuthToken {
        return oAuthService.socialSignUp(provider, code)
    }
}