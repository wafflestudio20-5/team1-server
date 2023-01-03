package com.wafflytime.board.controller

import com.wafflytime.board.dto.CreateBoardRequest
import com.wafflytime.board.dto.CreateBoardResponse
import com.wafflytime.board.dto.DeleteBoardResponse
import com.wafflytime.board.service.BoardService
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class BoardController(
    private val boardService: BoardService
) {

    @ExemptEmailVerification
    @PostMapping("/api/board")
    fun createBoard(@UserIdFromToken userId: Long, @Valid @RequestBody request: CreateBoardRequest) : ResponseEntity<CreateBoardResponse> {
        return ResponseEntity.ok().body(boardService.createBoard(userId, request))
    }

    @ExemptEmailVerification
    @DeleteMapping("/api/board/{boardId}")
    fun deleteBoard(@UserIdFromToken userId: Long, @PathVariable boardId: Long) : ResponseEntity<DeleteBoardResponse> {
        return ResponseEntity.ok().body(boardService.deleteBoard(userId, boardId))
    }


}