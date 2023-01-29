package com.wafflytime.post.dto

import com.wafflytime.common.DateTimeResponse
import com.wafflytime.post.database.PostEntity

data class PostResponse(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    val createdAt: DateTimeResponse,
    val writerId: Long,
    val nickname: String?,
    val isWriterAnonymous: Boolean,
    val isMyPost: Boolean,
    val isQuestion: Boolean,
    val title: String?,
    val contents: String,
    val images: List<ImageResponse>?,
    val nLikes: Int,
    val nScraps: Int,
    val nReplies: Int
) {
    companion object {

        fun of(userId: Long, postEntity: PostEntity) : PostResponse {
            return of(userId, postEntity, null)
        }

        fun of(userId: Long, postEntity: PostEntity, images: List<ImageResponse>?) : PostResponse {
            return PostResponse(
                boardId = postEntity.board.id,
                boardTitle = postEntity.board.title,
                postId = postEntity.id,
                createdAt = DateTimeResponse.of(postEntity.createdAt!!),
                writerId = postEntity.writer.id,
                nickname = if (postEntity.isWriterAnonymous) null else postEntity.writer.nickname,
                isWriterAnonymous = postEntity.isWriterAnonymous,
                isMyPost = userId == postEntity.writer.id,
                isQuestion = postEntity.isQuestion,
                title = postEntity.title,
                contents = postEntity.contents,
                images = images?.sortedBy { it.imageId },
                nLikes = postEntity.nLikes,
                nScraps = postEntity.nScraps,
                nReplies = postEntity.nReplies
            )
        }
    }
}
