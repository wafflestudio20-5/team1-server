package com.example.demo.dto

import jakarta.validation.constraints.NotEmpty

//import javax.validation.constraints.NotEmpty

data class ConfirmEmailRequest (
    @field:NotEmpty
    val email: String
)
