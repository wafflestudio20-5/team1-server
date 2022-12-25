package com.example.demo.service


import com.example.demo.dto.VerifyEmailCode
import com.example.demo.dto.VerifyEmailRequest
import com.example.demo.exception.WafflyTime400
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class EmailService(
    private val emailSender: JavaMailSender,
) {

    private val SNU_MAIL_SUFFIX = "@snu.ac.kr"
    private val CharPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun sendEmail(verifyEmailRequest: VerifyEmailRequest) : VerifyEmailCode {

        val verifyEmailCode: VerifyEmailCode = createCode()
        val preparator = MimeMessagePreparator {
            msg: MimeMessage? ->
            val helper = MimeMessageHelper(msg!!)
            helper.setTo(verifyEmailRequest.email)
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
            htmlMsgBuffer.append("${verifyEmailCode.code}</strong><div><br/> ")
            htmlMsgBuffer.append("</div>")

//            var html_msg = ""
//            html_msg += "<div style='margin:20px;'>"
//            html_msg += "<h1> Welcome WafflyTime. </h1>"
//            html_msg += "<br>"
//            html_msg += "<p>Please copy the code below<p>"
//            html_msg += "<br>"
//            html_msg += "<p> Thank you.<p>"
//            html_msg += "<br>"
//            html_msg += "<div align='center' style='border:1px solid black; font-family:verdana';>"
//            html_msg += "<h3 style='color:blue;'>This is mail verification code.</h3>"
//            html_msg += "<div style='font-size:130%'>"
//            html_msg += "CODE : <strong>"
//            html_msg += verifyEmailCode.code + "</strong><div><br/> "
//            html_msg += "</div>"
            helper.setText(htmlMsgBuffer.toString(), true)
        }
        emailSender.send(preparator)
        return verifyEmailCode
    }

    fun createCode() : VerifyEmailCode {
        return VerifyEmailCode(
            (1..8)
                .map { Random.nextInt(0, CharPool.size).let { CharPool[it] } }
                .joinToString("")
        )
    }

    fun verifyEmail(verifyEmailRequest: VerifyEmailRequest) : VerifyEmailCode {
        val email = verifyEmailRequest.email
        if (!email.endsWith(SNU_MAIL_SUFFIX)) {
            throw WafflyTime400("SNU 메일을 입력해주세요")
        }
        return sendEmail(verifyEmailRequest)
    }
}