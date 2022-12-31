package com.wafflytime.user.mail.api

import com.wafflytime.user.auth.controller.dto.AuthToken
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

    @PostMapping("/api/user/verify-mail")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest) : ResponseEntity<VerifyEmailCode> {
        // TODO(재웅) : 유저 관련 구현이 완료되면 유저 컨텍스트로 부터 id를 알아낼 수 있다.
        return ResponseEntity.ok().body(emailService.verifyEmail(request))
    }

    @PatchMapping("/api/user/verified-mail")
    fun patchUserMailVerified(@Valid @RequestBody request: VerifyEmailRequest) : ResponseEntity<AuthToken> {
        // TODO(재웅) : 유저 관련 구현이 완료되면 유저 컨텍스트로 부터 id를 알아낼 수 있다.
        val tmpUserId : Long = 1
        val user = userService.updateUserMailVerified(tmpUserId, request)

        return ResponseEntity.ok().body(
            authTokenService.buildAuthToken(user, LocalDateTime.now())
        )
    }
}