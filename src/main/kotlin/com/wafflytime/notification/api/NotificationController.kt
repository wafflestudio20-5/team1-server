package com.wafflytime.notification.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.notification.service.NotificationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class NotificationController(
    private val notificationService: NotificationService
) {
    // TODO: id가 아닌 access token으로 바꿔야 함 + exempt 제거
    @ExemptAuthentication
    @GetMapping(value = ["/api/sse-connect/{id}"], produces = ["text/event-stream"])
    fun connect(
        @PathVariable id: Long,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") lastEventId: String
    ) : SseEmitter {
        return notificationService.connect(id, lastEventId)
    }
}