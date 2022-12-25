package com.example.demo.api

import com.example.demo.dto.AuthToken
import com.example.demo.dto.VerifyEmailRequest
import com.example.demo.service.EmailService
import com.example.demo.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val emailService: EmailService,
    private val userService: UserService
) {

    @PostMapping("/api/user/verify-mail")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest) =
        ResponseEntity.ok().body(emailService.verifyEmail(request))

    @PatchMapping("/api/user/verified-mail")
    fun patchUserMailVerified() : ResponseEntity<AuthToken> {
        // TODO(재웅) : 유저 관련 구현이 완료되면 유저 컨텍스트로 부터 id를 알아낼 수 있다.
        val tmpUserId : Long = 1
        userService.updateUserMailVerified(tmpUserId)

        // TODO(재웅, 정민) : 이 함수에서 굳이 return 하지 않고, 프론트에서 이 patch mehtod를 호출한 이후부터는 프로트에서 알아서 AuthToken의 mailVerified 필드를 true로 바꿔서 보내줘도 될 듯
        return ResponseEntity.ok().body(
            AuthToken(accessToken = "user context에서 주어진 access token 그대로", mailVerified = true)
        )
    }
}