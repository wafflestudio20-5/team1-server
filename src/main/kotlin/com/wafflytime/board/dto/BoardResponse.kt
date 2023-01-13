package com.wafflytime.board.dto

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType

data class BoardResponse(
    val boardId: Long,
    val boardType: BoardType,
    val title: String,
    val description: String,
    val allowAnonymous: Boolean
) {
    companion object {
        fun of(board: BoardEntity) : BoardResponse {
            return BoardResponse(
                board.id,
                board.type,
                board.title,
                board.description,
                board.allowAnonymous,
            )
        }
    }
}
