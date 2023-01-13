package com.wafflytime.post.dto

import com.wafflytime.post.database.PostEntity

data class PostResponse(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    val writerId: Long,
    val nickname: String?,
    val isWriterAnonymous: Boolean,
    val isQuestion: Boolean,
    val title: String?,
    val contents: String,
    val images: List<ImageResponse>?,
    val nLikes: Int,
    val nScraps: Int
) {
    companion object {

        fun of(postEntity: PostEntity) : PostResponse {
            return of(postEntity, null)
        }

        fun of(postEntity: PostEntity, images: List<ImageResponse>?) : PostResponse {
            return PostResponse(
                boardId = postEntity.board.id,
                boardTitle = postEntity.board.title,
                postId = postEntity.id,
                writerId = postEntity.writer.id,
                nickname = if (postEntity.isWriterAnonymous) null else postEntity.writer.nickname,
                isWriterAnonymous = postEntity.isWriterAnonymous,
                isQuestion = postEntity.isQuestion,
                title = postEntity.title,
                contents = postEntity.contents,
                images = images?.sortedBy { it.imageId },
                nLikes = postEntity.nLikes,
                nScraps = postEntity.nScraps
            )
        }
    }
}
