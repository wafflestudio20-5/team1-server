package com.wafflytime.board.dto

import com.wafflytime.board.database.PostEntity

data class PostResponse(
    val postId: Long,
    val writerId: Long,
    val isWriterAnonymous: Boolean,
    val isQuestion: Boolean,
    val title: String?,
    val contents: String,
    val imageUrls: List<String>?
) {
    companion object {
        fun of(postEntity: PostEntity, preSignedUrls: List<String>?) : PostResponse {
            return PostResponse(
                postEntity.id,
                postEntity.writer.id,
                postEntity.isWriterAnonymous,
                postEntity.isQuestion,
                postEntity.title,
                postEntity.contents,
                imageUrls = preSignedUrls
            )
        }

        fun of(postEntity: PostEntity) : PostResponse {
            return of(postEntity, null)
        }
    }
}
