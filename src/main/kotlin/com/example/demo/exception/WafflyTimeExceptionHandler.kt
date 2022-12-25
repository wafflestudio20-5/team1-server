package com.example.demo.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class WafflyTimeExceptionHandler {

    @ExceptionHandler(value = [Exception::class])
    fun handle(e: Exception) : ResponseEntity<Map<String, Any>> {
        return createErrorInfo(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.message!!
        )
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(HttpStatus.BAD_REQUEST, "요청 형식이 잘못됐습니다(field가 누락됐거나 null 값이 들어왔습니다)")
    }

    @ExceptionHandler(value = [WafflyTimeException::class])
    fun handle(e: WafflyTimeException): ResponseEntity<Map<String, Any>> {
        return createErrorInfo(
            e.status, e.message!!
        )
    }

    private fun createErrorInfo(status: HttpStatus, error: String) : ResponseEntity<Map<String, Any>> {
        return ResponseEntity(
            mapOf(
                "timestamp" to LocalDateTime.now(),
                "status" to status.value(),
                "error" to error
            ),
            status
        )
    }
}