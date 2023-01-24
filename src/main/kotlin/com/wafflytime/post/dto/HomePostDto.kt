package com.wafflytime.post.dto

import com.wafflytime.common.DateTimeResponse

data class HomePostDto(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    val createdAt: DateTimeResponse,
    val title: String?,
    val contents: String,
    val images: List<ImageResponse>?,
    val nLikes: Int,
    val nScraps: Int,
    val nReplies: Int
) {
    companion object {
        fun of(redisPostDto: RedisPostDto, images: List<ImageResponse>?) : HomePostDto {
            return HomePostDto(
                boardId = redisPostDto.boardId,
                boardTitle = redisPostDto.boardTitle,
                postId = redisPostDto.postId,
                createdAt = redisPostDto.createdAt,
                title = redisPostDto.title,
                contents = redisPostDto.contents,
                images = images,
                nLikes = redisPostDto.nlikes,
                nScraps = redisPostDto.nscraps,
                nReplies = redisPostDto.nreplies
            )
        }
    }
}
