package com.wafflytime.board.dto

import com.wafflytime.board.database.PostEntity

data class PostResponse(
    val postId: Long,
    val writerId: Long,
    val isWriterAnonymous: Boolean,
    val isQuestion: Boolean,
    val title: String?,
    val contents: String,
    val images: List<ImageResponse>?
) {
    companion object {

        fun of(postEntity: PostEntity) : PostResponse {
            return of(postEntity, null)
        }

        fun of(postEntity: PostEntity, images: List<ImageResponse>?) : PostResponse {
            return PostResponse(
                postEntity.id,
                postEntity.writer.id,
                postEntity.isWriterAnonymous,
                postEntity.isQuestion,
                postEntity.title,
                postEntity.contents,
                images = images?.sortedBy { it.imageId }
            )
        }
    }
}
