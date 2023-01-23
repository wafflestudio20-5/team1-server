package com.wafflytime.board.api

import com.wafflytime.board.dto.*
import com.wafflytime.board.service.BoardService
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class BoardController(
    private val boardService: BoardService
) {

    @GetMapping("/api/board/{boardId}")
    fun getBoard(@PathVariable boardId: Long) : ResponseEntity<BoardResponse> {
        return ResponseEntity.ok().body(boardService.getBoard(boardId))
    }

    @GetMapping("/api/boards")
    fun getBoards() : ResponseEntity<List<BoardListResponse>> {
        // boards 는 적어서 pageable 할 필요 없음
        return ResponseEntity.ok().body(boardService.getAllBoards())
    }

    @PostMapping("/api/board")
    fun createBoard(@UserIdFromToken userId: Long, @Valid @RequestBody request: CreateBoardRequest) : ResponseEntity<CreateBoardResponse> {
        return ResponseEntity.ok().body(boardService.createBoard(userId, request))
    }

    @DeleteMapping("/api/board/{boardId}")
    fun deleteBoard(@UserIdFromToken userId: Long, @PathVariable boardId: Long) : ResponseEntity<DeleteBoardResponse> {
        return ResponseEntity.ok().body(boardService.deleteBoard(userId, boardId))
    }

    @GetMapping("/api/homeboards")
    fun getHomePost() : ResponseEntity<List<HomeBoardResponse>> {
        return ResponseEntity.ok(boardService.getHomeBoards())
    }

    @GetMapping("/api/boards/search")
    fun searchBoards(@RequestParam(required = true, value = "keyword") keyword: String) : ResponseEntity<List<BoardResponse>> {
        return ResponseEntity.ok(boardService.searchBoards(keyword))
    }
}