package com.wafflytime.user.mail.api

import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.user.auth.api.dto.AuthToken
import com.wafflytime.user.mail.api.dto.VerifyEmailCode
import com.wafflytime.user.mail.api.dto.VerifyEmailRequest
import com.wafflytime.user.auth.service.AuthTokenService
import com.wafflytime.user.mail.service.EmailService
import com.wafflytime.user.info.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class EmailVerificationController(
    private val emailService: EmailService,
    private val userService: UserService,
    private val authTokenService: AuthTokenService,
) {

    @ExemptEmailVerification
    @PostMapping("/api/user/verify-mail")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest) : ResponseEntity<VerifyEmailCode> {
        return ResponseEntity.ok().body(emailService.verifyEmail(request))
    }

    @ExemptEmailVerification
    @PatchMapping("/api/user/verified-mail")
    fun patchUserMailVerified(@UserIdFromToken userId: Long, @Valid @RequestBody request: VerifyEmailRequest) : ResponseEntity<AuthToken> {
        val user = userService.updateUserMailVerified(userId, request)

        return ResponseEntity.ok().body(
            authTokenService.buildAuthToken(user, LocalDateTime.now())
        )
    }
}