package com.wafflytime.user.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class TempAdminSignUpRequest(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val password: String,
    @field:NotBlank
    val univEmail: String
)
