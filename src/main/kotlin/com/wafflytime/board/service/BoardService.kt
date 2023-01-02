package com.wafflytime.board.service

import com.wafflytime.board.database.BoardRepository
import org.springframework.stereotype.Service


@Service
class BoardService(
    private val boardRepository: BoardRepository
) {
}