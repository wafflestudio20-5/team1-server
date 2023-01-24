package com.wafflytime.user.mail.service

import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.service.AuthTokenService
import com.wafflytime.user.mail.dto.VerifyEmailRequest
import com.wafflytime.user.info.service.UserService
import com.wafflytime.user.mail.database.MailVerificationEntity
import com.wafflytime.user.mail.database.MailVerificationRepository
import com.wafflytime.user.mail.dto.VerifyEmailCode
import com.wafflytime.user.mail.exception.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class EmailService(
    private val asyncEmailService: AsyncEmailService,
    private val authTokenService: AuthTokenService,
    private val userService: UserService,
    private val mailVerificationRepository: MailVerificationRepository,
) {
    private val SNU_MAIL_SUFFIX = "@snu.ac.kr"
    private val CharPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val verificationSecond : Long = 190

    fun createCode() : String {
        return (1..8)
            .map { Random.nextInt(0, CharPool.size).let { CharPool[it] } }
            .joinToString("")
    }

    @Transactional
    fun verifyEmail(userId: Long, verifyEmailRequest: VerifyEmailRequest) {
        val email = verifyEmailRequest.email
        if (!email.endsWith(SNU_MAIL_SUFFIX)) {
            throw InvalidMailSuffix
        }

        userService.getUser(userId).univEmail?.let { throw AlreadyMailVerified }
        userService.checkUnivEmailConflict(email)

        val mailVerification = mailVerificationRepository.findByUserId(userId)?.let {
            it.code = createCode()
            it.email = verifyEmailRequest.email
            it
        } ?: mailVerificationRepository.save(
            MailVerificationEntity(
                userId,
                createCode(),
                verifyEmailRequest.email,
            )
        )

        asyncEmailService.sendEmail(mailVerification.email, mailVerification.code)
    }

    @Transactional
    fun completeVerification(userId: Long, request: VerifyEmailCode) : AuthToken {
        val now = LocalDateTime.now()

        val mailVerification = mailVerificationRepository.findByUserId(userId)
            ?: throw VerificationNotStarted

        if (now <= mailVerification.modifiedAt!!.plusSeconds(verificationSecond)) {
            if (request.code == mailVerification.code) {
                val user = userService.updateUserMailVerified(userId, mailVerification.email)

                mailVerificationRepository.delete(mailVerification)
                return authTokenService.buildAuthToken(user, LocalDateTime.now())
            } else {
                mailVerificationRepository.delete(mailVerification)
                throw WrongVerificationCode
            }
        } else {
            mailVerificationRepository.delete(mailVerification)
            throw VerificationTimeOver
        }
    }

}