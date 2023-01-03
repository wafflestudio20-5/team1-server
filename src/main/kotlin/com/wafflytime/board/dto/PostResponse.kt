package com.wafflytime.board.dto

import com.wafflytime.board.database.PostEntity

data class PostResponse(
    val postId: Long,
    val writerId: Long,
    val isWriterAnonymous: Boolean,
    val title: String,
    val contents: String,
) {
    companion object {
        fun of(postEntity: PostEntity) : PostResponse {
            return PostResponse(
                postEntity.id,
                postEntity.writer.id,
                postEntity.isWriterAnonymous,
                postEntity.title,
                postEntity.contents
            )
        }
    }
}
