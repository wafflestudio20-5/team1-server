package com.wafflytime.user.mail.exception

import com.wafflytime.exception.MailException
import org.springframework.http.HttpStatus

open class Mail400(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Mail401(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Mail403(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Mail404(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.NOT_MODIFIED)
open class Mail409(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.CONFLICT)

object InvalidMailSuffix : Mail400("학교 메일을 입력해주세요", 0)
object AlreadyMailVerified : Mail409("이미 이메일 인증을 완료한 유저입니다", 1)
object VerificationNotStarted : Mail400("유효하지 않은 이메일 인증입니다", 2)
object WrongVerificationCode : Mail400("인증코드가 틀렸습니다", 3)
object VerificationTimeOver : Mail400("이메일 인증 유효 시간이 지났습니다", 4)