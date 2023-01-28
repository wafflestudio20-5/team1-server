package com.wafflytime.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.dto.WebSocketReceiveMessage
import com.wafflytime.chat.dto.WebSocketSendMessage
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
        if (jwtExpirationFromAttribute(session) > LocalDateTime.now()) {
            session.close(CloseStatus(9900, "토큰 인증시간 만료"))
            return
        }

        val userId = userIdFromAttribute(session)
        val (chatId, contents) = convertToJson(message)
        val chat = chatService.getChatEntity(chatId)
        val (sender, receiver) = chat.getSenderAndReceiver(userId)

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
            )
        } else {
            session.sendMessage(
                convertToTextMessage(WebSocketReceiveMessage.of(userId, messageEntity))
            )
        }
    }

    private fun userIdFromAttribute(session: WebSocketSession): Long {
        return session.attributes["UserIdFromToken"] as? Long
            ?: throw TODO()
    }

    private fun jwtExpirationFromAttribute(session: WebSocketSession): LocalDateTime {
        return session.attributes["JwtExpiration"] as? LocalDateTime
            ?: throw TODO()
    }

    private fun convertToJson(message: TextMessage): WebSocketSendMessage {
        return objectMapper.readValue(message.payload, WebSocketSendMessage::class.java)
    }

    private fun convertToTextMessage(messageInfo: WebSocketReceiveMessage): TextMessage {
        return TextMessage(objectMapper.writeValueAsString(messageInfo))
    }

}