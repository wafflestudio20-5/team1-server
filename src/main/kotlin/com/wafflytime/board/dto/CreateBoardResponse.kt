package com.wafflytime.board.dto

import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType

data class CreateBoardResponse(
    val userId: Long,
    val boardId: Long,
    val boardType: BoardType,
    val category: BoardCategory,
    val title: String,
    val description: String,
    val allowAnonymous: Boolean
)
