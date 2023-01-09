package com.wafflytime.board.dto

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.type.BoardCategory



data class BoardListResponse(
    val id: Int,
    val category: BoardCategory,
    val size: Int,
    val defaultDisplayColumnSize: Int,
    val boards: List<BoardItem>?
) {

    data class BoardItem(
        val id: Int,
        val name: String,
    )

    companion object {
        fun of(category: BoardCategory, boards: List<BoardEntity>?) : BoardListResponse {
            return BoardListResponse(
                id = category.ordinal,
                category = category,
                size = boards?.size ?: 0,
                defaultDisplayColumnSize = category.defaultDisplayColumnSize,
                boards = boards?.toMutableList()?.mapIndexed { index, boardEntity ->  BoardItem(id=index, boardEntity.title)}
            )
        }
    }
}
