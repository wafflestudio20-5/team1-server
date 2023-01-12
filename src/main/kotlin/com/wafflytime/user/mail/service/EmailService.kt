package com.wafflytime.user.mail.service

import com.wafflytime.user.mail.dto.VerifyEmailCode
import com.wafflytime.user.mail.dto.VerifyEmailRequest
import com.wafflytime.user.info.service.UserService
import com.wafflytime.user.mail.exception.InvalidMailSuffix
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class EmailService(
    private val asyncEmailService: AsyncEmailService,
    private val userService: UserService,
) {
    private val SNU_MAIL_SUFFIX = "@snu.ac.kr"
    private val CharPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

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
            throw InvalidMailSuffix
        }

        userService.checkUnivEmailConflict(email)

        val verifyEmailCode = createCode()
        asyncEmailService.sendEmail(verifyEmailRequest.email, verifyEmailCode)
        return verifyEmailCode
    }
}