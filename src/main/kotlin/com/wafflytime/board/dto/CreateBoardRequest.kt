package com.wafflytime.board.dto

import com.wafflytime.board.types.BoardType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateBoardRequest(
    val univEmail: String? = null,
    @NotBlank
    val title: String,

    @NotNull
    val boardType: BoardType
)
