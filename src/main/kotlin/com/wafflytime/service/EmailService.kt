package com.wafflytime.service

import com.wafflytime.database.UserRepository
import com.wafflytime.dto.VerifyEmailCode
import com.wafflytime.dto.VerifyEmailRequest
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime409
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class EmailService(
    private val asyncEmailService: AsyncEmailService,
    private val userRepository: UserRepository,
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
            throw WafflyTime400("SNU 메일을 입력해주세요")
        }

        userRepository.findBySnuMail(email)?.let { throw WafflyTime409("이미 이 snu mail로 가입한 게정이 존재합니다") }

        val verifyEmailCode = createCode()
        asyncEmailService.sendEmail(verifyEmailRequest.email, verifyEmailCode)
        return verifyEmailCode
    }
}