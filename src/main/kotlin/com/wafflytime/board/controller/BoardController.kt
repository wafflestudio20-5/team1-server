package com.wafflytime.board.controller

import com.wafflytime.board.dto.CreateBoardRequest
import com.wafflytime.board.dto.CreateBoardResponse
import com.wafflytime.board.dto.DeleteBoardRequest
import com.wafflytime.board.dto.DeleteBoardResponse
import com.wafflytime.board.service.BoardService
import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class BoardController(
    private val boardService: BoardService
) {

    /**
     * univEmail과 title을 넘겨주면 univEmail을 통해 유저를 찾아내서 board를 생성한다

     * 훗날 시간이 된다면 일반 유저와 관리자 유저를 구분 지어서 작동하게 할 수 있을 것 같다. 일단은 request에 univEmil이 null 값이 들어오면
       관리자가 생성하는 게시판이라 가정해본다
     */
    @ExemptEmailVerification
    @PostMapping("/api/board")
    fun createBoard(@UserIdFromToken userId: Long, @Valid @RequestBody request: CreateBoardRequest) : ResponseEntity<CreateBoardResponse> {
        return ResponseEntity.ok().body(boardService.createBoard(userId, request))
    }

    /**
     * 일단 게시판 생성과 마찬가지로 delete를 위해 univEmail을 담아서 보낸다고 가정 + 관리자가 삭제하는 api를 날린다고 가정
     * 이 경우 역시 univEmail이 null인 경우 관리자라 가정
     */
    @ExemptEmailVerification
    @DeleteMapping("/api/board/{boardId}")
    fun deleteBoard(@UserIdFromToken userId: Long, @PathVariable boardId: Long) : ResponseEntity<DeleteBoardResponse> {
        return ResponseEntity.ok().body(boardService.deleteBoard(userId, boardId))
    }


}