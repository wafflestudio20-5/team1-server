package com.wafflytime.post.dto

import com.wafflytime.board.database.BoardEntity

data class HomePostResponse(
    val boardId: Long,
    val boardTitle: String,
    val hasPostTitle: Boolean,
    val posts : List<PostResponse>
) {
    companion object {
        fun of(board: BoardEntity, posts: List<PostResponse>) : HomePostResponse{
            return HomePostResponse(
                boardId = board.id,
                boardTitle = board.title,
                hasPostTitle = !board.type.name.startsWith("CUSTOM"),
                posts = posts,
            )
        }
    }
}
