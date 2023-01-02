package com.wafflytime.board.controller

import com.wafflytime.board.service.BoardService
import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController(
    private val boardService: BoardService
) {

    @ExemptAuthentication
    @GetMapping("/api/boards")
    fun getTest() : String {
        return "hello board controller"
    }
}