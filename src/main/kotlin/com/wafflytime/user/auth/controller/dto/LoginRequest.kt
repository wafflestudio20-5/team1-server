package com.wafflytime.user.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val password: String,
)