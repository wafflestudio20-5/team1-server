package com.wafflytime.notification.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.notification.dto.CheckNotificationResponse
import com.wafflytime.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping(value = ["/api/sse-connect"], produces = ["text/event-stream"])
    fun connect(
        @UserIdFromToken userId: Long,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") lastEventId: String
    ) : SseEmitter {
        return notificationService.connect(userId, lastEventId)
    }


    /**
     * TODO : 백엔드 팀의 자체 테스트를 위한 테스트 api로 merge 하기 전에 지우자
     * javascript 기본 EventSource 는 accessToken을 Header에 담을 수 없어서 아래와 같은 임시 api
     */
    @ExemptAuthentication
    @GetMapping(value = ["/api/sse-connect/{userId}"], produces = ["text/event-stream"])
    fun connectTest(
        @PathVariable userId: Long,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") lastEventId: String
    ) : SseEmitter {
        return notificationService.connect(userId, lastEventId)
    }

    @PutMapping("/api/notification-check/{notificationId}")
    fun checkNotification(@PathVariable notificationId: Long) : ResponseEntity<CheckNotificationResponse>{
        return ResponseEntity.ok(notificationService.checkNotification(notificationId))
    }
}