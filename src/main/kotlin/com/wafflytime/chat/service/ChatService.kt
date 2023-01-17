package com.wafflytime.chat.service

import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.database.ChatRepository
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.database.MessageRepository
import com.wafflytime.chat.dto.ChatSimpleInfo
import com.wafflytime.chat.dto.CreateChatRequest
import com.wafflytime.chat.dto.MessageInfo
import com.wafflytime.chat.dto.SendMessageRequest
import com.wafflytime.chat.exception.ChatNotFound
import com.wafflytime.chat.exception.NoMoreUnreadMessages
import com.wafflytime.chat.exception.UserChatMismatch
import com.wafflytime.post.service.PostService
import com.wafflytime.reply.service.ReplyService
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.Integer.max
import java.time.format.DateTimeFormatter

interface ChatService {
    fun createChat(userId: Long, sourceBoardId: Long, sourcePostId: Long, sourceReplyId: Long? = null, request: CreateChatRequest): ChatSimpleInfo
    fun sendMessage(userId: Long, chatId: Long, request: SendMessageRequest): MessageInfo
    fun getChats(userId: Long): List<ChatSimpleInfo>
    fun getMessages(userId: Long, chatId: Long, page: Int, size: Int?): Page<MessageInfo>
}

@Service
class ChatServiceImpl(
    private val userService: UserService,
    private val postService: PostService,
    private val replyService: ReplyService,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
): ChatService {

    @Transactional
    override fun createChat(userId: Long, sourceBoardId: Long, sourcePostId: Long, sourceReplyId: Long?, request: CreateChatRequest): ChatSimpleInfo {
        val user = userService.getUser(userId)
        val sourcePost = postService.validateBoardAndPost(sourceBoardId, sourcePostId)
        val sourceBoard = sourcePost.board
        val sourceReply = sourceReplyId?.let {
            replyService.getReplyEntity(sourcePostId, it)
        }

        val target: UserEntity
        val isTargetAnonymous: Boolean
        if (sourceReply == null) {
            target = sourcePost.writer
            isTargetAnonymous = sourcePost.isWriterAnonymous
        } else {
            target = sourceReply.writer
            isTargetAnonymous = sourceReply.isWriterAnonymous
        }

        val chat =
            if (!request.isAnonymous && !isTargetAnonymous) {
                // 둘다 익명이 아닌 경우 서로의 아이디만 확인
                chatRepository.findByBothParticipantId(user.id, target.id)
            } else {
                // 둘 중 한명이라도 익명인 경우 게시물과 익명 여부에도 의존적
                chatRepository.findByAllConditions(
                    sourcePostId,
                    user.id,
                    request.isAnonymous,
                    target.id,
                    isTargetAnonymous
                )
            } ?: createChat(
                // 똑같은 아이덴티티로 보낸 쪽지가 존재하지 않는 경우 새 채팅방 생성
                sourcePostId, user, request.isAnonymous, target, isTargetAnonymous,
                "${sourceBoard.title}에 ${
                    DateTimeFormatter.ofPattern("MM/DD hh:mm").format(sourcePost.createdAt)
                } 작성된 글을 통해 온 쪽지입니다.",
            )

        val firstMessage = messageRepository.save(
            MessageEntity(chat, user, request.content)
        )
        chat.addMessage(firstMessage)

        return ChatSimpleInfo.of(userId, chat)
    }

    @Transactional
    override fun sendMessage(userId: Long, chatId: Long, request: SendMessageRequest): MessageInfo {
        val user = userService.getUser(userId)
        val chat = getChatEntity(chatId)
        validateChatParticipant(user, chat)

        val message = messageRepository.save(
            MessageEntity(chat, user, request.content)
        )
        chat.addMessage(message)

        return MessageInfo.of(userId, message)
    }

    @Transactional
    override fun getChats(userId: Long): List<ChatSimpleInfo> {
        return chatRepository.findByParticipantIdWithLastMessage(userId)
            .map { ChatSimpleInfo.of(userId, it) }
    }

    @Transactional
    override fun getMessages(userId: Long, chatId: Long, page: Int, size: Int?): Page<MessageInfo> {
        val chat = getChatEntity(chatId)
        val defaultSize: Int
        chat.run {
            defaultSize = when (userId) {
                participant1.id -> unread1
                participant2.id -> unread2
                else -> throw UserChatMismatch
            }
        }

        val size = size ?: defaultSize
        if (size == 0) throw NoMoreUnreadMessages
        chat.run {
            when (userId) {
                participant1.id -> unread1 = max(0, unread1 - size)
                participant2.id -> unread2 = max(0, unread2 - size)
            }
        }

        val pageRequest = PageRequest.of(page, size)
        return messageRepository.findByChatIdPageable(chatId, pageRequest)
            .map { MessageInfo.of(userId, it) }
    }

    private fun createChat(postId: Long, participant1: UserEntity, isAnonymous1: Boolean, participant2: UserEntity, isAnonymous2: Boolean, systemMessageContent: String): ChatEntity {
        val chat = chatRepository.save(
            ChatEntity(
                postId = postId,
                participant1 = participant1, isAnonymous1 = isAnonymous1,
                participant2 = participant2, isAnonymous2 = isAnonymous2,
            )
        )

        val systemMessage = messageRepository.save(
            MessageEntity(chat = chat, content = systemMessageContent)
        )
        chat.addMessage(systemMessage)

        return chat
    }

    private fun getChatEntity(chatId: Long): ChatEntity {
        return chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFound
    }

    private fun validateChatParticipant(user: UserEntity, chat: ChatEntity) {
        if (chat.participant1 != user && chat.participant2 != user)
            throw UserChatMismatch
    }

}