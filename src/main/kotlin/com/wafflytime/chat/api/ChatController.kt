package com.wafflytime.chat.api

import com.wafflytime.chat.dto.*
import com.wafflytime.chat.service.ChatService
import com.wafflytime.common.CursorPage
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
class ChatController(
    private val chatService: ChatService,
) {

    @PostMapping("/api/board/{boardId}/post/{postId}/chat")
    fun createChat(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestParam(required = false, value = "replyId") replyId: Long?,
        @Valid @RequestBody request: CreateChatRequest,
    ): CreateChatResponse {
        return chatService.createChat(userId, boardId, postId, replyId, request)
    }

    @GetMapping("/api/chat")
    fun getChat(
        @UserIdFromToken userId: Long,
        @RequestParam(required = true, value = "chatId") chatId: Long,
    ): ChatSimpleInfo {
        return chatService.getChat(userId, chatId)
    }

    @GetMapping("/api/chats")
    fun getChatList(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page") page: Long?,
        @RequestParam(required = false, value = "cursor") cursor: Long?,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Long,
    ): CursorPage<ChatSimpleInfo> {
        return page?.let { chatService.getChats(userId, it, size) }
            ?: chatService.getChats(userId, cursor, size)
    }

    @GetMapping("/api/chat/{chatId}/messages")
    fun getMessages(
        @UserIdFromToken userId: Long,
        @PathVariable chatId: Long,
        @RequestParam(required = false, value = "page") page: Long?,
        @RequestParam(required = false, value = "cursor") cursor: Long?,
        @RequestParam(required = false, value = "size") size: Long?,
    ): CursorPage<MessageInfo> {
        return page?.let { chatService.getMessages(userId, chatId, it, size) }
            ?: chatService.getMessages(userId, chatId, cursor, size)
    }

    @PutMapping("/api/chat/{chatId}")
    fun updateChatBlock(
        @UserIdFromToken userId: Long,
        @PathVariable chatId: Long,
        @Valid @RequestBody request: UpdateChatBlockRequest,
    ): ChatSimpleInfo {
        return chatService.updateChatBlock(userId, chatId, request)
    }

    @PutMapping("/api/chat/unread")
    fun updateChatUnread(
        @UserIdFromToken userId: Long,
        @Valid @RequestBody request: UpdateUnreadRequest,
    ): String {
        chatService.updateUnread(userId, request)
        return "success"
    }

}