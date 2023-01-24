package com.wafflytime.chat.exception

import com.wafflytime.exception.ChatException
import org.springframework.http.HttpStatus

open class Chat400(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Chat401(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Chat403(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Chat404(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Chat409(msg: String, errorCode: Int) : ChatException(msg, errorCode, HttpStatus.CONFLICT)

object ChatNotFound : Chat404("해당 id의 채팅을 찾을 수 없습니다", 0)
object UserChatMismatch : Chat403("해당 유저가 속한 채팅이 아닙니다", 1)
object NoMoreUnreadMessages : Chat400("안 읽은 메세지가 남아있지 않습니다", 2)
object SelfChatForbidden : Chat400("자신에게 채팅을 보낼 수 없습니다", 3)
object AlreadyBlocked : Chat400("이미 차단한 채팅입니다", 4)
object AlreadyUnblocked : Chat400("이미 차단 해제한 채팅입니다", 5)