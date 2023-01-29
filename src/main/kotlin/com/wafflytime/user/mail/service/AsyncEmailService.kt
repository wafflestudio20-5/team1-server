package com.wafflytime.user.mail.service

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsyncEmailService(
    private val emailSender: JavaMailSender
) {

    @Async("mailExecutor")
    fun sendEmail(email:String, verifyEmailCode: String) {

        val preparator = MimeMessagePreparator {
                msg: MimeMessage? ->
            val helper = MimeMessageHelper(msg!!)
            helper.setTo(email)
            helper.setSubject("WafflyTime 인증번호입니다")

            val htmlMsgBuffer = StringBuffer()
            htmlMsgBuffer.append("<div style='margin:20px;'>")
            htmlMsgBuffer.append("<h1> Welcome WafflyTime. </h1>")
            htmlMsgBuffer.append("<br>")
            htmlMsgBuffer.append("<p>Please copy the code below<p>")
            htmlMsgBuffer.append("<br>")
            htmlMsgBuffer.append("<p> Thank you.<p>")
            htmlMsgBuffer.append("<br>")
            htmlMsgBuffer.append("<div align='center' style='border:1px solid black; font-family:verdana';>")
            htmlMsgBuffer.append("<h3 style='color:blue;'>This is mail verification code.</h3>")
            htmlMsgBuffer.append("<div style='font-size:130%'>")
            htmlMsgBuffer.append("CODE : <strong>")
            htmlMsgBuffer.append("${verifyEmailCode}</strong><div><br/> ")
            htmlMsgBuffer.append("</div>")
            helper.setText(htmlMsgBuffer.toString(), true)
        }
        emailSender.send(preparator)
    }
}