package com.wafflytime.exception

import org.springframework.http.HttpStatus

open class WafflyTimeException(msg: String, val errorCode: Int = 0, val status: HttpStatus) : RuntimeException(msg)

open class AuthException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 100 + errorCode, status)
open class UserInfoException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 200 + errorCode, status)
open class MailException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 300 + errorCode, status)
open class BoardException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 400 + errorCode, status)
open class PostException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 500 + errorCode, status)

//class WafflyTime400(msg: String) : WafflyTimeException(msg, HttpStatus.BAD_REQUEST)
//class WafflyTime401(msg: String) : WafflyTimeException(msg, HttpStatus.UNAUTHORIZED)
//class WafflyTime403(msg: String) : WafflyTimeException(msg, HttpStatus.FORBIDDEN)
//class WafflyTime404(msg: String) : WafflyTimeException(msg, HttpStatus.NOT_FOUND)
//class WafflyTime409(msg: String) : WafflyTimeException(msg, HttpStatus.CONFLICT)
//
//class WafflyTime500(msg: String) : WafflyTimeException(msg, HttpStatus.INTERNAL_SERVER_ERROR)