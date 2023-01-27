package com.wafflytime.exception

import org.springframework.http.HttpStatus

open class WafflyTimeException(msg: String, val errorCode: Int = 0, val status: HttpStatus) : RuntimeException(msg)

open class AuthException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 100 + errorCode, status)
open class UserInfoException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 200 + errorCode, status)
open class MailException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 300 + errorCode, status)
open class BoardException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 400 + errorCode, status)
open class PostException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 500 + errorCode, status)
open class ReplyException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 600 + errorCode, status)
open class NotificationException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 700 + errorCode, status)
open class ChatException(msg: String, errorCode: Int, status: HttpStatus) : WafflyTimeException(msg, 800 + errorCode, status)