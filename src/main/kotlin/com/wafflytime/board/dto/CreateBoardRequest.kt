package com.wafflytime.board.dto

import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateBoardRequest(
    @NotBlank
    val title: String,
    @NotNull
    val boardType: BoardType,
    val boardCategory: BoardCategory = BoardCategory.OTHER,
    val allowAnonymous: Boolean = true,
    val description: String = ""
)
