package com.wafflytime.board.dto

import com.wafflytime.board.database.BoardEntity

data class BoardResponse(
    val boardId: Long,
    val title: String,
    val description: String
) {
    companion object {
        fun of(board: BoardEntity) : BoardResponse {
            return BoardResponse(board.id, board.title, board.description)
        }
    }
}
