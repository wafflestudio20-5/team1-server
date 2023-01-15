package com.wafflytime.user.auth.dto

import jakarta.validation.constraints.NotBlank

data class SignUpRequest(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val password: String,
    @field:NotBlank
    val nickname: String,
)