package com.wafflytime.user.auth.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class SocialSignUpRequest(
    @field:NotBlank
    @field:Length(min = 2, max = 10)
    val nickname: String,
)
