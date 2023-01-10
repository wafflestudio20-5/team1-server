package com.wafflytime.user.info.exception

import com.wafflytime.exception.UserInfoException
import org.springframework.http.HttpStatus

open class UserInfo400(msg: String, errorCode: Int) : UserInfoException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class UserInfo401(msg: String, errorCode: Int) : UserInfoException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class UserInfo403(msg: String, errorCode: Int) : UserInfoException(msg, errorCode, HttpStatus.FORBIDDEN)
open class UserInfo404(msg: String, errorCode: Int) : UserInfoException(msg, errorCode, HttpStatus.NOT_FOUND)
open class UserInfo409(msg: String, errorCode: Int) : UserInfoException(msg, errorCode, HttpStatus.CONFLICT)

object UserNotFound : UserInfo404("해당 id의 유저가 존재하지 않습니다", 0)
object LoginIdConflict : UserInfo409("이미 사용중인 아이디입니다", 1)
object NicknameConflict : UserInfo409("이미 사용중인 닉네임입니다", 2)
object MailConflict : UserInfo409("이미 해당 학교 메일로 인증한 계정이 존재합니다", 3)