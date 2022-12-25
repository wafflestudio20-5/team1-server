package com.example.demo.service

import com.example.demo.dto.ConfirmEmailRequest

interface EmailService {
    fun sendEmail(confirmEmailRequest: ConfirmEmailRequest)
}