package com.wafflytime.chat.api

import com.wafflytime.chat.dto.*
import com.wafflytime.chat.service.ChatService
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.data.domain.Page
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
    fun getChatList(
        @UserIdFromToken userId: Long,
    ): List<ChatSimpleInfo> {
        return chatService.getChats(userId)
    }

    @GetMapping("/api/chat/{chatId}/messages")
    fun getMessages(
        @UserIdFromToken userId: Long,
        @PathVariable chatId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size") size: Int?,
    ): Page<MessageInfo> {
        return chatService.getMessages(userId, chatId, page, size)
    }

    @PutMapping("/api/chat/{chatId}")
    fun updateChatBlock(
        @UserIdFromToken userId: Long,
        @PathVariable chatId: Long,
        @Valid @RequestBody request: UpdateChatBlockRequest,
    ): ChatSimpleInfo {
        return chatService.updateChatBlock(userId, chatId, request)
    }

}