package com.wafflytime.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import java.time.LocalDateTime

@RestControllerAdvice
class WafflyTimeExceptionHandler {

    @ExceptionHandler(value = [Exception::class])
    fun handle(e: Exception) : ResponseEntity<Map<String, Any>> {
        return createErrorInfo(
            HttpStatus.INTERNAL_SERVER_ERROR,
            0,
            e.message!!
        )
    }

    @ExceptionHandler(value = [AsyncRequestTimeoutException::class])
    fun handle(e: AsyncRequestTimeoutException) {
        // SseEmitter 가 timeout 될 때 나는 exception 으로 아무것도 리턴하지 않고 무시하면 된다
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(HttpStatus.BAD_REQUEST, 1, "요청 형식이 잘못됐습니다(field가 누락됐거나 null 값이 들어왔습니다)")
    }

    @ExceptionHandler(value = [HttpRequestMethodNotSupportedException::class])
    fun handle(e: HttpRequestMethodNotSupportedException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(HttpStatus.NOT_FOUND, 2, "해당 URI는 ${e.method} 메소드의 요청이 존재하지 않습니다.")
    }

    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handle(e: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(HttpStatus.BAD_REQUEST, 3, "요청 형식이 잘못됐습니다(json body 형식 에러)")
    }

    @ExceptionHandler(value = [WafflyTimeException::class])
    fun handle(e: WafflyTimeException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(
            e.status, e.errorCode, e.message!!
        )
    }

    private fun createErrorInfo(status: HttpStatus, errorCode: Int, defaultMessage: String) : ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to status.value(),
                "error-code" to errorCode,
                "default-message" to defaultMessage
            ),
            status
        )
    }
}