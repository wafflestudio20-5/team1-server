package com.wafflytime.user.auth.dto

import jakarta.validation.constraints.NotBlank

data class SocialSignUpRequest(
    @field:NotBlank
    val nickname: String,
)
