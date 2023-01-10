package com.wafflytime.board.api

import com.wafflytime.board.dto.CreateReplyRequest
import com.wafflytime.board.dto.ReplyResponse
import com.wafflytime.board.dto.UpdateReplyRequest
import com.wafflytime.board.service.ReplyService
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
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @PathVariable replyId: Long,
    ) : ResponseEntity<ReplyResponse>{
        return ResponseEntity.ok(replyService.getReply(boardId, postId, replyId))
    }

    @GetMapping("/api/board/{boardId}/post/{postId}/replies")
    fun getReplies(
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Long,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Long,
    ) : ResponseEntity<List<ReplyResponse>>{
        return ResponseEntity.ok(replyService.getReplies(boardId, postId, page, size))
    }
}