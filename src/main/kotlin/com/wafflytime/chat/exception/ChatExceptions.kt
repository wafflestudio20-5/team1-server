package com.wafflytime.chat.exception

import com.wafflytime.exception.ChatException
import org.springframework.http.HttpStatus

open class Chat400(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Chat401(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Chat403(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Chat404(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Chat409(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.CONFLICT)

object ChatNotFound : Chat404("해당 id의 채팅방을 찾을 수 없습니다", 0)
object UserChatMismatch : Chat403("해당 유저가 속한 채팅방이 아닙니다", 1)
object NoMoreUnreadMessages : Chat400("안 읽은 메세지가 남아있지 않습니다", 2)