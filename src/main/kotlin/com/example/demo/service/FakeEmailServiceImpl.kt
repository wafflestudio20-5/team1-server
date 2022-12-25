package com.example.demo.service

import com.example.demo.dto.ConfirmEmailRequest
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Service

@Service
class FakeEmailServiceImpl(
    private val emailSender: JavaMailSender
): EmailService {

    override fun sendEmail(confirmEmailRequest: ConfirmEmailRequest) {
        val preparator = MimeMessagePreparator {
            msg: MimeMessage? ->
            val helper = MimeMessageHelper(msg!!)
            helper.setTo(confirmEmailRequest.email)
            helper.setSubject("WafflyTime 인증번호입니다")
            helper.setText("인증번호: 1234")
        }
        emailSender.send(preparator)
    }
}