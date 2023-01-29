package com.wafflytime.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.database.ChatRepository
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.database.MessageRepository
import com.wafflytime.chat.dto.WebSocketChatCreationInfo
import com.wafflytime.chat.dto.WebSocketServerMessage
import com.wafflytime.chat.dto.WebSocketClientMessage
import com.wafflytime.chat.exception.ChatNotFound
import com.wafflytime.chat.exception.UserChatMismatch
import com.wafflytime.chat.exception.WebsocketAttributeError
import com.wafflytime.notification.dto.NotificationDto
import com.wafflytime.notification.service.NotificationService
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.time.LocalDateTime

interface WebSocketService {
    fun addSession(session: WebSocketSession)
    fun removeSession(session: WebSocketSession)
    fun sendMessage(session: WebSocketSession, message: TextMessage)
    fun sendCreateChatResponse(chat: ChatEntity, systemMessage: MessageEntity?, firstMessage: MessageEntity)
}

@Service
class WebSocketServiceImpl(
    private val notificationService: NotificationService,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) : WebSocketService {

    private val webSocketSessions: MutableMap<Long, WebSocketSession> = mutableMapOf()
    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun addSession(session: WebSocketSession) {
        webSocketSessions[userIdFromAttribute(session)] = session
    }

    override fun removeSession(session: WebSocketSession) {
        webSocketSessions.remove(userIdFromAttribute(session))
    }

    @Transactional
    override fun sendMessage(session: WebSocketSession, message: TextMessage) {
        val expiration = try {
            jwtExpirationFromAttribute(session)
        } catch (e: WebsocketAttributeError) {
            session.close(CloseStatus(9900, e.message))
            return
        }
        if (expiration < LocalDateTime.now()) {
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
        val chat = getChatEntity(chatId)
        val (sender, receiver) = try {
            chat.getSenderAndReceiver(userId)
        } catch (e: UserChatMismatch) {
            session.close(CloseStatus(9902, e.message))
            return
        }

        val messageEntity = MessageEntity(chat, sender, contents)

        if (!chat.isBlocked()) {
            val (toSender, toReceiver) = WebSocketServerMessage.senderAndReceiverPair(
                saveMessage(chat, messageEntity)
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
                convertToTextMessage(WebSocketServerMessage.of(userId, messageEntity))
            )
        }
    }

    @Transactional
    override fun sendCreateChatResponse(chat: ChatEntity, systemMessage: MessageEntity?, firstMessage: MessageEntity) {
        val senderSession = webSocketSessions[chat.participant1.id]
        val receiverSession = webSocketSessions[chat.participant2.id]

        if (senderSession == null && receiverSession == null) return

        if (systemMessage != null) {
            val chatCreationInfo = convertToTextMessage(WebSocketChatCreationInfo.of(chat))
            val systemMessageToBoth = WebSocketServerMessage.of(chat.participant1.id, systemMessage)

            senderSession?.run {
                sendMessage(chatCreationInfo)
                sendMessage(convertToTextMessage(systemMessageToBoth))
            }
            receiverSession?.run {
                sendMessage(chatCreationInfo)
                sendMessage(convertToTextMessage(systemMessageToBoth))
            }
        }

        val (firstMessageToSender, firstMessageToReceiver) = WebSocketServerMessage.senderAndReceiverPair(firstMessage)
        senderSession?.run {
            sendMessage(convertToTextMessage(firstMessageToSender))
        }
        receiverSession?.run {
            sendMessage(convertToTextMessage(firstMessageToReceiver))
        } ?: run {
            notificationService.send(
                NotificationDto.fromMessage(chat.participant2, firstMessage)
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

    private fun getChatEntity(chatId: Long): ChatEntity {
        return chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFound
    }

    private fun saveMessage(chat: ChatEntity, message: MessageEntity): MessageEntity {
        val messageEntity = messageRepository.save(message)
        chat.addMessage(messageEntity)
        return messageEntity
    }

    private fun convertToJson(message: TextMessage): WebSocketClientMessage {
        return objectMapper.readValue(message.payload, WebSocketClientMessage::class.java)
    }

    private fun convertToTextMessage(serverMessage: WebSocketServerMessage): TextMessage {
        return TextMessage(objectMapper.writeValueAsString(serverMessage))
    }

    private fun convertToTextMessage(chatCreationInfo: WebSocketChatCreationInfo): TextMessage {
        return TextMessage(objectMapper.writeValueAsString(chatCreationInfo))
    }

}