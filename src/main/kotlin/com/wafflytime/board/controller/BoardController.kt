package com.wafflytime.board.controller

import com.wafflytime.board.dto.CreateBoardRequest
import com.wafflytime.board.dto.CreateBoardResponse
import com.wafflytime.board.dto.DeleteBoardRequest
import com.wafflytime.board.dto.DeleteBoardResponse
import com.wafflytime.board.service.BoardService
import com.wafflytime.config.ExemptAuthentication
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController(
    private val boardService: BoardService
) {

    /**
     * 일반 유저는 게시판을 만들지 못하는 것 같다. 유저가 게시판을 만들고 싶다고 관리자에게 연락하면 관리자가 게시판을 만들어 주는 구조인듯
       현재는 일단 일반 유저가 게시판을 만드는 것은 불가능하다고 가정하자.

     * univEmail과 title을 넘겨주면 univEmail을 통해 유저를 찾아내서 board를 생성한다

     * 훗날 시간이 된다면 일반 유저와 관리자 유저를 구분 지어서 작동하게 할 수 있을 것 같다. 일단은 request에 univEmil이 null 값이 들어오면
       관리자가 생성하는 게시판이라 가정해본다
     */
    @ExemptAuthentication
    @PostMapping("/api/board")
    fun createBoard(@Valid @RequestBody request: CreateBoardRequest) : ResponseEntity<CreateBoardResponse> {
        return ResponseEntity.ok().body(boardService.createBoard(request))
    }

    /**
     * 게시판 생성을 해 본 적이 없어 게시판 소유자에게는 게시판 삭제 버튼이 보여지는건지, 삭제를 위해서는 관리자에게 따로 연락을 해야 되는 건지 아직 모름
     * 일단 게시판 생성과 마찬가지로 delete를 위해 univEmail을 담아서 보낸다고 가정 + 관리자가 삭제하는 api를 날린다고 가정
     * 이 경우 역시 univEmail이 null인 경우 관리자라 가정
     */
    @ExemptAuthentication
    @DeleteMapping("/api/board")
    fun deleteBoard(@Valid @RequestBody request: DeleteBoardRequest) : ResponseEntity<DeleteBoardResponse> {
        return ResponseEntity.ok().body(boardService.deleteBoard(request))
    }


}