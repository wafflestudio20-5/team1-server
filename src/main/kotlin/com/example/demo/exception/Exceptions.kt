package com.example.demo.exception

import org.springframework.http.HttpStatus


open class WafflyTimeException(msg: String, val status: HttpStatus) : RuntimeException(msg)

class WafflyTime400(msg: String) : WafflyTimeException(msg, HttpStatus.BAD_REQUEST)
class WafflyTime401(msg: String) : WafflyTimeException(msg, HttpStatus.UNAUTHORIZED)
class WafflyTime403(msg: String) : WafflyTimeException(msg, HttpStatus.FORBIDDEN)
class WafflyTime404(msg: String) : WafflyTimeException(msg, HttpStatus.NOT_FOUND)
class WafflyTime409(msg: String) : WafflyTimeException(msg, HttpStatus.CONFLICT)

class WafflyTime500(msg: String) : WafflyTimeException(msg, HttpStatus.INTERNAL_SERVER_ERROR)