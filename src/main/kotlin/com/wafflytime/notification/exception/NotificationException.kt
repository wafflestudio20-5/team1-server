package com.wafflytime.notification.exception

import com.wafflytime.exception.NotificationException
import org.springframework.http.HttpStatus

open class Notification404(msg: String, errorCode: Int) : NotificationException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Notification501(msg: String, errorCode: Int) : NotificationException(msg, errorCode, HttpStatus.NOT_IMPLEMENTED)


object NotificationNotFound : Notification404("해당 notification id를 찾을 수 없습니다", 0)
object NotificationInfoNotImplemented : Notification404("해당 notification info 타입에 대해 json converter가 아직 구현되지 않았습니다", 1)
