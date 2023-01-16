package com.wafflytime.chat.api

import com.wafflytime.chat.dto.ChatSimpleInfo
import com.wafflytime.chat.dto.CreateChatRequest
import com.wafflytime.chat.dto.MessageInfo
import com.wafflytime.chat.dto.SendMessageRequest
import com.wafflytime.chat.service.ChatService
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
    ): ChatSimpleInfo {
        return chatService.createChat(userId, boardId, postId, replyId, request)
    }

    @PostMapping("/api/chat/{chatId}")
    fun sendMessage(
        @UserIdFromToken userId: Long,
        @PathVariable chatId: Long,
        @Valid @RequestBody request: SendMessageRequest,
    ): MessageInfo {
        return chatService.sendMessage(userId, chatId, request)
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

}