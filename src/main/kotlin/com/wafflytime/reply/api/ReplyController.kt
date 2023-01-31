package com.wafflytime.reply.api

import com.wafflytime.common.DoubleCursorPage
import com.wafflytime.reply.dto.CreateReplyRequest
import com.wafflytime.reply.dto.ReplyResponse
import com.wafflytime.reply.dto.UpdateReplyRequest
import com.wafflytime.reply.service.ReplyService
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ReplyController(
    val replyService: ReplyService,
) {

    @PostMapping("/api/board/{boardId}/post/{postId}/reply")
    fun createReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @Valid @RequestBody request: CreateReplyRequest,
    ): ResponseEntity<ReplyResponse> {
        return ResponseEntity.ok(replyService.createReply(userId, boardId, postId, request))
    }

    @PutMapping("/api/board/{boardId}/post/{postId}/reply/{replyId}")
    fun updateReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
        @Valid @RequestBody request: UpdateReplyRequest
    ): ResponseEntity<ReplyResponse> {
        return ResponseEntity.ok(replyService.updateReply(userId, boardId, postId, replyId, request))
    }

    @DeleteMapping("/api/board/{boardId}/post/{postId}/reply/{replyId}")
    fun deleteReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
    ) {
        replyService.deleteReply(userId, boardId, postId, replyId)
    }

    @GetMapping("/api/board/{boardId}/post/{postId}/reply/{replyId}")
    fun getReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
    ) : ResponseEntity<ReplyResponse>{
        return ResponseEntity.ok(replyService.getReply(userId, boardId, postId, replyId))
    }

    @GetMapping("/api/board/{boardId}/post/{postId}/replies")
    fun getReplies(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestParam(required = false, value = "first") first: Long?,
        @RequestParam(required = false, value = "second") second: Long?,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Long,
    ) : ResponseEntity<DoubleCursorPage<ReplyResponse>>{
        return ResponseEntity.ok(replyService.getReplies(boardId, postId, first, second, size))
    }

    @PostMapping("/api/board/{boardId}/post/{postId}/reply/{replyId}/like")
    fun likeReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
    ): ResponseEntity<ReplyResponse> {
        return ResponseEntity.ok(replyService.likeReply(userId, boardId, postId, replyId))
    }

}