package com.wafflytime.notification.service

import com.wafflytime.notification.database.EmitterRepository
import com.wafflytime.notification.database.NotificationRepository
import io.jsonwebtoken.io.IOException
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import kotlin.system.exitProcess

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val emitterRepository: EmitterRepository
) {

    private val DEFAULT_TIMEOUT = 60L * 1000 * 30

    private fun getTimeIncludedId(userId: Long) : String {
        return "${userId}_${System.currentTimeMillis()}"
    }

    fun connect(userId: Long, lastEventId: String): SseEmitter {
        val emitterId = getTimeIncludedId(userId)
        val emitter = emitterRepository.save(emitterId, SseEmitter(DEFAULT_TIMEOUT))

        emitter.onCompletion {
            emitterRepository.deleteByEmitterId(emitterId)
        }
        emitter.onTimeout {
            emitterRepository.deleteByEmitterId(emitterId)
        }

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        val eventId = getTimeIncludedId(userId)
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=${userId}]")


        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실 예방
        if (lastEventId.isNotEmpty()) {
            emitterRepository.findAllEventCacheStartWithByUserId(userId)
                .filter { lastEventId < it.key }
                .forEach { sendNotification(emitter, it.key, emitterId, it.value) }
        }

        return emitter
    }

    private fun sendNotification(emitter: SseEmitter, eventId: String, emitterId: String, data: Any) {
        try {
            emitter.send(
                SseEmitter.event().id(eventId).name("sse").data(data)
            )
        } catch (exception:IOException) {
            emitterRepository.deleteByEmitterId(emitterId)
        }
    }
}