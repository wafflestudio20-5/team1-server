package com.wafflytime.user.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class SocialSignUpRequest(
    @field:NotBlank
    val socialEmail: String,
    @field:NotBlank
    val univEmail: String,
)