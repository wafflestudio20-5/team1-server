package com.wafflytime.notification.database

import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap


public interface EmitterRepository {
    fun save(emitterId: String, sseEmitter: SseEmitter) : SseEmitter
    fun saveEventCache(eventCacheId: String, event: Any)
    fun findAllEmitterStartWithByUserId(userId: Long) : Map<String, SseEmitter>
    fun findAllEventCacheStartWithByUserId(userId: Long) : Map<String, Any>
    fun deleteByEmitterId(emitterId: String)
    fun deleteAllEmitterStartWithId(userId: Long)
    fun deleteAllEventCacheStartWithId(userId: Long)
}

@Repository
class EmitterRepositoryImpl(
    private val emitters: ConcurrentHashMap<String, SseEmitter> = ConcurrentHashMap(),
    private val eventCache: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
) : EmitterRepository {

    override fun save(emitterId: String, sseEmitter: SseEmitter): SseEmitter {
        emitters[emitterId] = sseEmitter
        return sseEmitter
    }

    override fun saveEventCache(eventCacheId: String, event: Any) {
        eventCache[eventCacheId] = event
    }

    override fun findAllEmitterStartWithByUserId(userId: Long): Map<String, SseEmitter> {
        return emitters.filter { it.key.startsWith(userId.toString()) }
    }

    override fun findAllEventCacheStartWithByUserId(userId: Long): Map<String, Any> {
        return eventCache.filter { it.key.startsWith(userId.toString()) }
    }

    override fun deleteByEmitterId(emitterId: String) {
        emitters.remove(emitterId)
    }

    override fun deleteAllEmitterStartWithId(userId: Long) {
        emitters.forEach {
            if (it.key.startsWith(userId.toString())) {
                emitters.remove(it.key)
            }
        }
    }

    override fun deleteAllEventCacheStartWithId(userId: Long) {
        eventCache.forEach {
            if (it.key.startsWith(userId.toString())) {
                eventCache.remove(it.key)
            }
        }
    }
}