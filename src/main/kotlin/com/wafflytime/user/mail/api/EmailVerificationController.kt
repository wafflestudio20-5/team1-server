package com.wafflytime.user.mail.api

import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.mail.dto.VerifyEmailRequest
import com.wafflytime.user.mail.service.EmailService
import com.wafflytime.user.mail.dto.VerifyEmailCode
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EmailVerificationController(
    private val emailService: EmailService,
) {

    @ExemptEmailVerification
    @PostMapping("/api/user/verify-mail")
    fun verifyEmail(@UserIdFromToken userId: Long, @Valid @RequestBody request: VerifyEmailRequest) : ResponseEntity<String> {
        emailService.verifyEmail(userId, request)
        return ResponseEntity.ok().body("success")
    }

    @ExemptEmailVerification
    @PatchMapping("/api/user/verify-mail")
    fun patchUserMailVerified(@UserIdFromToken userId: Long, @Valid @RequestBody request: VerifyEmailCode) : ResponseEntity<AuthToken> {
        return ResponseEntity.ok().body(emailService.completeVerification(userId, request))
    }
}