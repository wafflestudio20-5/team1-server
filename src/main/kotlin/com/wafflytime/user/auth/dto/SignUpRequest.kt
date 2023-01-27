package com.wafflytime.user.auth.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class SignUpRequest(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val password: String,
    @field:NotBlank
    @field:Length(min = 2, max = 10)
    val nickname: String,
)