package com.wafflytime.board.dto

import com.wafflytime.board.database.BoardEntity

data class HomeBoardResponse(
    val boardId: Long,
    val boardTitle: String,
    val hasPostTitle: Boolean
) {
    companion object {
        fun of(board: BoardEntity) : HomeBoardResponse {
            return HomeBoardResponse(
                boardId = board.id,
                boardTitle = board.title,
                hasPostTitle = board.type.name.startsWith("CUSTOM")
            )
        }
    }
}