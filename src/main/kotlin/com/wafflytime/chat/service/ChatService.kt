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

        val chat = if (sourceReply == null) {
            chatRepository.save(
                ChatEntity(
                    participant1 = user, isAnonymous1 = request.isAnonymous,
                    participant2 = sourcePost.writer, isAnonymous2 = sourcePost.isWriterAnonymous,
                )
            )
        } else {
            chatRepository.save(
                ChatEntity(
                    participant1 = user, isAnonymous1 = request.isAnonymous,
                    participant2 = sourceReply.writer, isAnonymous2 = sourceReply.isWriterAnonymous,
                )
            )
        }

        val infoMessage = messageRepository.save(
            MessageEntity(
                chat = chat,
                content = "${sourceBoard.title}에 ${DateTimeFormatter.ofPattern("MM/DD hh:mm").format(sourcePost.createdAt)} 작성된 글을 통해 온 쪽지입니다.",
            )
        )
        val firstMessage = messageRepository.save(
            MessageEntity(
                chat = chat,
                content = request.content
            )
        )
        chat.addMessage(infoMessage)
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
            when (userId) {
                participant1.id -> {
                    defaultSize = unread1
                    unread1 = 0
                }
                participant2.id -> {
                    defaultSize = unread2
                    unread2 = 0
                }
                else -> throw UserChatMismatch
            }
        }

        val size = size ?: defaultSize
        if (size == 0) throw NoMoreUnreadMessages

        val pageRequest = PageRequest.of(page, size)
        return messageRepository.findByChatIdPageable(chatId, pageRequest)
            .map { MessageInfo.of(userId, it) }
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