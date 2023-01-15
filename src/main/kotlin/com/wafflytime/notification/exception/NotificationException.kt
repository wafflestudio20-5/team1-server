package com.wafflytime.notification.exception

import com.wafflytime.exception.NotificationException
import org.springframework.http.HttpStatus

open class Notification404(msg: String, errorCode: Int) : NotificationException(msg, errorCode, HttpStatus.NOT_FOUND)

object NotificationNotFound : Notification404("해당 notification id를 찾을 수 없습니다", 0)
