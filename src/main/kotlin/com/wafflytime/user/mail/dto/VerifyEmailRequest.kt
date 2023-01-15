package com.wafflytime.user.mail.dto

import jakarta.validation.constraints.NotEmpty


data class VerifyEmailRequest (
    @field:NotEmpty
    val email: String
)
