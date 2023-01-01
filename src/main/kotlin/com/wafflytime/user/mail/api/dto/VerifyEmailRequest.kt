package com.wafflytime.user.mail.api.dto

import jakarta.validation.constraints.NotEmpty


data class VerifyEmailRequest (
    @field:NotEmpty
    val email: String
)
