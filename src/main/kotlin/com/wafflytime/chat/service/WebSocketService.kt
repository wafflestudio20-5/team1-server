package com.wafflytime.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.dto.WebSocketReceiveMessage
import com.wafflytime.chat.dto.WebSocketSendMessage
import com.wafflytime.chat.exception.UserChatMismatch
import com.wafflytime.chat.exception.WebsocketAttributeError
import com.wafflytime.notification.dto.NotificationDto
import com.wafflytime.notification.service.NotificationService
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.time.LocalDateTime

interface WebSocketService {
    fun addSession(session: WebSocketSession)
    fun removeSession(session: WebSocketSession)
    fun sendMessage(session: WebSocketSession, message: TextMessage)
}

@Service
class WebSocketServiceImpl(
    private val chatService: ChatService,
    private val notificationService: NotificationService,
) : WebSocketService {

    private val webSocketSessions: MutableMap<Long, WebSocketSession> = mutableMapOf()
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun addSession(session: WebSocketSession) {
        webSocketSessions[userIdFromAttribute(session)] = session
    }

    override fun removeSession(session: WebSocketSession) {
        webSocketSessions.remove(userIdFromAttribute(session))
    }

    override fun sendMessage(session: WebSocketSession, message: TextMessage) {
        val expiration = try {
            jwtExpirationFromAttribute(session)
        } catch (e: WebsocketAttributeError) {
            session.close(CloseStatus(9900, e.message))
            return
        }
        if (expiration > LocalDateTime.now()) {
            session.close(CloseStatus(9901, "토큰 인증시간 만료"))
            return
        }

        val userId = try {
            userIdFromAttribute(session)
        } catch (e: WebsocketAttributeError) {
            session.close(CloseStatus(9900, e.message))
            return
        }
        val (chatId, contents) = convertToJson(message)
        val chat = chatService.getChatEntity(chatId)
        val (sender, receiver) = try {
            chat.getSenderAndReceiver(userId)
        } catch (e: UserChatMismatch) {
            session.close(CloseStatus(9902, e.message))
            return
        }

        val messageEntity = MessageEntity(chat, sender, contents)

        if (!chat.isBlocked()) {
            val (toSender, toReceiver) = WebSocketReceiveMessage.senderAndReceiverPair(
                chatService.saveMessage(chat, messageEntity)
            )

            session.sendMessage(
                convertToTextMessage(toSender)
            )
            webSocketSessions[receiver.id]?.sendMessage(
                convertToTextMessage(toReceiver)
            ) ?: run {
                notificationService.send(
                    NotificationDto.fromMessage(receiver, messageEntity)
                )
            }
        } else {
            session.sendMessage(
                convertToTextMessage(WebSocketReceiveMessage.of(userId, messageEntity))
            )
        }
    }

    private fun userIdFromAttribute(session: WebSocketSession): Long {
        return session.attributes["UserIdFromToken"] as? Long
            ?: throw WebsocketAttributeError
    }

    private fun jwtExpirationFromAttribute(session: WebSocketSession): LocalDateTime {
        return session.attributes["JwtExpiration"] as? LocalDateTime
            ?: throw WebsocketAttributeError
    }

    private fun convertToJson(message: TextMessage): WebSocketSendMessage {
        return objectMapper.readValue(message.payload, WebSocketSendMessage::class.java)
    }

    private fun convertToTextMessage(messageInfo: WebSocketReceiveMessage): TextMessage {
        return TextMessage(objectMapper.writeValueAsString(messageInfo))
    }

}