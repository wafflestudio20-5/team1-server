package com.wafflytime.user.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class SocialLoginRequest(
    @field:NotBlank
    val socialEmail: String,
)