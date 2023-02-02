package com.wafflytime.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.database.ChatRepository
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.database.MessageRepository
import com.wafflytime.chat.dto.WebSocketChatCreationInfo
import com.wafflytime.chat.dto.WebSocketServerMessage
import com.wafflytime.chat.dto.WebSocketClientMessage
import com.wafflytime.chat.dto.WebSocketUpdateRequired
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

enum class WsCloseStatus(
    private val code: Int,
    private val reason: String,
) {
    ATTRIBUTE(4900, "웹소켓 session attribute 문제"),
    EXPIRED(4901, "토큰 인증시간 만료"),
    MISMATCH(4902, "해당 유저가 속한 채팅이 아닙니다"),
    NEWSESSION(4903, "다른 기기에서 연결되어 접속이 종료됩니다");

    fun value() = CloseStatus(code, reason)
}


interface WebSocketService {
    fun addSession(session: WebSocketSession)
    fun removeSession(session: WebSocketSession)
    fun sendMessage(session: WebSocketSession, message: TextMessage)
    fun sendCreateMessageResponse(userId: Long, chat: ChatEntity, message: MessageEntity)
    fun sendCreateChatResponse(userId: Long, chat: ChatEntity, systemMessage: MessageEntity?, firstMessage: MessageEntity)
    fun sendUpdateRequiredResponse(userId: Long, chatId: List<Long>, unread: List<Int>)
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
        val userId = userIdFromAttribute(session)
        webSocketSessions[userId]?.run {
            close(WsCloseStatus.NEWSESSION.value())
        }

        webSocketSessions[userId] = session
    }

    override fun removeSession(session: WebSocketSession) {
        webSocketSessions.remove(userIdFromAttribute(session))
    }

    @Transactional
    override fun sendMessage(session: WebSocketSession, message: TextMessage) {
        if (!isSessionValid(session)) return

        val userId = try {
            userIdFromAttribute(session)
        } catch (e: WebsocketAttributeError) {
            session.close(WsCloseStatus.ATTRIBUTE.value())
            return
        }
        val (chatId, contents) = convertToJson(message)
        val chat = getChatEntity(chatId)
        val (sender, receiver) = try {
            chat.getSenderAndReceiver(userId)
        } catch (e: UserChatMismatch) {
            session.close(WsCloseStatus.MISMATCH.value())
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
            getWebSocketSession(receiver.id)?.sendMessage(
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

    // rest api로 메세지가 생성됐을 때 열려있는 웹소켓으로 보내주기
    @Transactional
    override fun sendCreateMessageResponse(userId: Long, chat: ChatEntity, message: MessageEntity) {
        val session1 = getWebSocketSession(chat.participant1.id)
        val session2 = getWebSocketSession(chat.participant2.id)
        val (senderSession, receiverSession) = when (userId) {
            chat.participant1.id -> Pair(session1, session2)
            chat.participant2.id -> Pair(session2, session1)
            else -> throw UserChatMismatch
        }

        val (toSender, toReceiver) = WebSocketServerMessage.senderAndReceiverPair(message)
        senderSession?.run {
            sendMessage(convertToTextMessage(toSender))
        }
        receiverSession?.run {
            sendMessage(convertToTextMessage(toReceiver))
        }
    }

    @Transactional
    override fun sendCreateChatResponse(userId: Long, chat: ChatEntity, systemMessage: MessageEntity?, firstMessage: MessageEntity) {
        val session1 = getWebSocketSession(chat.participant1.id)
        val session2 = getWebSocketSession(chat.participant2.id)
        val (senderSession, receiverSession) = when (userId) {
            chat.participant1.id -> Pair(session1, session2)
            chat.participant2.id -> Pair(session2, session1)
            else -> throw UserChatMismatch
        }

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

    override fun sendUpdateRequiredResponse(userId: Long, chatId: List<Long>, unread: List<Int>) {
        webSocketSessions[userId]?.run {
            sendMessage(convertToTextMessage(WebSocketUpdateRequired(chatId, unread)))
        }
    }

    private fun getWebSocketSession(userId: Long): WebSocketSession? {
        val session = webSocketSessions[userId]
        session?.let { isSessionValid(it) }
        return session
    }

    private fun isSessionValid(session: WebSocketSession): Boolean {
        val expiration = try {
            jwtExpirationFromAttribute(session)
        } catch (e: WebsocketAttributeError) {
            session.close(WsCloseStatus.ATTRIBUTE.value())
            return false
        }

        if (expiration < LocalDateTime.now()) {
            session.close(WsCloseStatus.EXPIRED.value())
            return false
        }

        return true
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

    private fun convertToTextMessage(updateRequired: WebSocketUpdateRequired): TextMessage {
        return TextMessage(objectMapper.writeValueAsString(updateRequired))
    }

}