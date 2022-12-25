package com.example.demo.api

import com.example.demo.dto.ConfirmEmailRequest
import com.example.demo.service.EmailService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val emailService: EmailService
) {

    @PostMapping("/api/user/mail-confirm")
    fun confirmMail(@Valid @RequestBody request: ConfirmEmailRequest) : Any {
        emailService.sendEmail(request)
        return "succeed"
    }
}