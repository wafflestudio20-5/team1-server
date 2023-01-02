package com.wafflytime.board.dto

import com.wafflytime.board.types.BoardType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DeleteBoardRequest(
    val univEmail: String? = null,
    @NotBlank
    val title: String
)
