package com.wafflytime.notification.service

import com.wafflytime.notification.database.EmitterRepository
import com.wafflytime.notification.database.NotificationEntity
import com.wafflytime.notification.database.NotificationRepository
import com.wafflytime.notification.dto.CheckNotificationResponse
import com.wafflytime.notification.dto.NotificationDto
import com.wafflytime.notification.dto.NotificationResponse
import com.wafflytime.notification.exception.NotificationNotFound
import jakarta.transaction.Transactional
import org.apache.catalina.connector.ClientAbortException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

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

    @Transactional
    fun send(notificationDto: NotificationDto) {
        val notification = notificationRepository.save(
            NotificationEntity(
                info =  notificationDto.notificationInfo,
                receiver = notificationDto.receiver,
                content = notificationDto.content,
                contentCreatedAt = notificationDto.contentCreatedAt,
                notificationType = notificationDto.notificationType,
                isRead = false
            )
        )

        val eventId = getTimeIncludedId(notificationDto.receiver.id)
        val emittersMap = emitterRepository.findAllEmitterStartWithByUserId(userId = notificationDto.receiver.id)
        emittersMap.forEach {
            emitterRepository.saveEventCache(it.key, notification)
            sendNotification(
                emitter = it.value,
                eventId = eventId,
                emitterId = it.key,
                data = NotificationResponse.of(notification)
            )
        }

    }

    private fun sendNotification(emitter: SseEmitter, eventId: String, emitterId: String, data: Any) {
        try {
            emitter.send(
                SseEmitter.event().id(eventId).name("sse").data(data)
            )
        } catch (exception: ClientAbortException) {
            emitterRepository.deleteByEmitterId(emitterId)
        }
    }

    @Transactional
    fun checkNotification(notificationId: Long): CheckNotificationResponse {
        val notification = notificationRepository.findByIdOrNull(notificationId) ?: throw NotificationNotFound
        notification.updateIsRead()
        return CheckNotificationResponse(notificationId)
    }

    fun getNotifications(userId: Long, page: Int, size: Int): Page<NotificationResponse> {
        return notificationRepository.findAllByReceiverId(
            userId, PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map { NotificationResponse.of(it) }
    }
}