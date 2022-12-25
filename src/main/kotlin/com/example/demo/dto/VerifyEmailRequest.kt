package com.example.demo.dto

import jakarta.validation.constraints.NotEmpty


data class VerifyEmailRequest (
    @field:NotEmpty
    val email: String
)
