package com.wafflytime.post.dto

import com.wafflytime.common.DateTimeResponse
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.database.image.ImageColumn

data class RedisPostDto(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    val title: String?,
    var contents: String,
    var images: Map<String, ImageColumn>?,
    val createdAt: DateTimeResponse,

    // Redis 저장할 때 n 으로 시작하는 camel case는 redis가 맘대로 바꿔버린다... + "is"로 시작하는 이름은 아예 is를 제거해버림
    var nlikes: Int,
    var nreplies: Int
) {
    constructor() : this(-1, "", -1, "", "", null, DateTimeResponse(), -1, -1)

    companion object {
        fun of(postEntity: PostEntity) : RedisPostDto {
            return RedisPostDto(
                postEntity.board.id,
                postEntity.board.title,
                postEntity.id,
                postEntity.title,
                postEntity.contents,
                postEntity.images,
                DateTimeResponse.of(postEntity.createdAt!!),
                postEntity.nLikes,
                postEntity.nReplies
            )
        }
    }
}
