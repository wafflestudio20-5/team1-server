package com.wafflytime.user.mail.exception

import com.wafflytime.exception.MailException
import org.springframework.http.HttpStatus

open class Mail400(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Mail401(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Mail403(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Mail404(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.NOT_MODIFIED)
open class Mail409(msg: String, errorCode: Int) : MailException(msg, errorCode, HttpStatus.CONFLICT)

object InvalidMailSuffix : Mail400("학교 메일을 입력해주세요", 0)