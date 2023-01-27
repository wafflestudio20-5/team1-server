package com.wafflytime.board.dto

import com.wafflytime.board.type.BoardType
import com.wafflytime.post.dto.HomePostDto


data class HomePostResponse(
    val boardId: Long,
    val boardTitle: String,
    val hasPostTitle: Boolean,
    val posts : List<HomePostDto>
) {
    companion object {

        fun of(boardId: Long, boardTitle: String, boardType: BoardType, posts: List<HomePostDto>) : HomePostResponse {
            return HomePostResponse(
                boardId = boardId,
                boardTitle = boardTitle,
                hasPostTitle = !boardType.name.startsWith("CUSTOM"),
                posts = posts
            )
        }
    }
}