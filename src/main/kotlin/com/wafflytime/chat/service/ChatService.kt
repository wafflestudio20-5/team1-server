package com.wafflytime.chat.service

import com.wafflytime.chat.database.ChatEntity
import com.wafflytime.chat.database.ChatRepository
import com.wafflytime.chat.database.MessageEntity
import com.wafflytime.chat.database.MessageRepository
import com.wafflytime.chat.dto.*
import com.wafflytime.chat.exception.*
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.service.PostService
import com.wafflytime.reply.database.ReplyEntity
import com.wafflytime.reply.service.ReplyService
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

interface ChatService {
    fun createChat(userId: Long, sourceBoardId: Long, sourcePostId: Long, sourceReplyId: Long? = null, request: CreateChatRequest): CreateChatResponse
    fun getChats(userId: Long): List<ChatSimpleInfo>
    fun getMessages(userId: Long, chatId: Long, page: Int, size: Int?): Page<MessageInfo>
    fun updateChatBlock(userId: Long, chatId: Long, request: UpdateChatBlockRequest): ChatSimpleInfo
}

@Service
class ChatServiceImpl(
    private val userService: UserService,
    private val postService: PostService,
    private val replyService: ReplyService,
    private val webSocketService: WebSocketService,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
): ChatService {

    @Transactional
    override fun createChat(userId: Long, sourceBoardId: Long, sourcePostId: Long, sourceReplyId: Long?, request: CreateChatRequest): CreateChatResponse {
        val user = userService.getUser(userId)
        val sourcePost = postService.validateBoardAndPost(sourceBoardId, sourcePostId)
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

        if (user == target) throw SelfChatForbidden

        // 채팅방 아이덴티티를 바탕으로 기존 채팅방 존재 여부 검색
        val existingChat =
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
            }

        val systemMessage: MessageEntity?
        val chat = if (existingChat == null) {
            // 주어진 아이덴티티의 채팅 방이 존재하는 경우 새 채팅방 생성
            val newChat = chatRepository.save(
                ChatEntity(
                    postId = sourcePostId,
                    participant1 = user, isAnonymous1 = request.isAnonymous,
                    participant2 = target, isAnonymous2 = isTargetAnonymous,
                )
            )

            systemMessage = sendMessage(
                chat = newChat,
                contents = buildSystemMessage(sourcePost, sourceReply),
            )

            newChat
        } else {
            systemMessage = null
            existingChat
        }

        val firstMessage = sendMessage(chat, user, request.content)

        webSocketService.sendCreateChatResponse(chat, systemMessage, firstMessage)

        return CreateChatResponse(
            systemMessage != null,
            ChatSimpleInfo.of(userId, chat),
            systemMessage?.let { MessageInfo.of(userId, it)},
            MessageInfo.of(userId, firstMessage),
        )
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

    @Transactional
    override fun updateChatBlock(userId: Long, chatId: Long, request: UpdateChatBlockRequest): ChatSimpleInfo {
        val chat = getChatEntity(chatId)
        chat.run {
            when (userId) {
                participant1.id -> blocked1 = when (request.block) {
                    true -> if (blocked1) throw AlreadyBlocked else true
                    false -> if (blocked1) false else throw AlreadyUnblocked
                }
                participant2.id -> blocked2 = when (request.block) {
                    true -> if (blocked2) throw AlreadyBlocked else true
                    false -> if (blocked2) false else throw AlreadyUnblocked
                }
            }
        }

        return ChatSimpleInfo.of(userId, chat)
    }

    private fun getChatEntity(chatId: Long): ChatEntity {
        return chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFound
    }

    private fun sendMessage(chat: ChatEntity, sender: UserEntity? = null, contents: String): MessageEntity {
        val message = MessageEntity(chat, sender, contents)

        return if (!chat.isBlocked()) {
            val message = messageRepository.save(message)
            chat.addMessage(message)
            message
        } else {
            message
        }
    }

    private fun buildSystemMessage(post: PostEntity, reply: ReplyEntity? = null): String {
        return if (reply == null) {
            "${post.board.title}에 작성된 글을 통해 시작된 쪽지 입니다.\n" +
                    "글 내용: ${post.title ?: post.contents}"
        } else {
            "${post.board.title}에 작성된 ${if (reply.isWriterAnonymous) "익명"+reply.anonymousId else reply.writer.nickname}의 댓글을 통해 시작된 쪽지입니다.\n" +
                    "글 내용: ${post.title ?: post.contents}"
        }
    }

}