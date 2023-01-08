package com.wafflytime.board.api

import com.wafflytime.board.dto.*
import com.wafflytime.board.service.ReplyService
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ReplyController(
    val replyService: ReplyService,
) {

    @ExemptEmailVerification
    @PostMapping("/api/board/{boardId}/post/{postId}/reply")
    fun createReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @Valid @RequestBody request: CreateReplyRequest,
    ): ResponseEntity<ReplyResponse> {
        return ResponseEntity.ok(replyService.createReply(userId, boardId, postId, request))
    }

    @ExemptEmailVerification
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

    @ExemptEmailVerification
    @DeleteMapping("/api/board/{boardId}/post/{postId}/reply/{replyId}")
    fun deleteReply(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
    ) {
        replyService.deleteReply(userId, boardId, postId, replyId)
    }
}