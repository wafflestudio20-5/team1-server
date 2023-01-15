package com.wafflytime.post.database.image

import com.wafflytime.post.dto.S3PostImageUrlDto

data class ImageColumn(
    val imageId: Int,
    val s3Url: String,
    val description: String?
) {
    // 반드시 default 부생성자가 필요함 - 지우면 안됨
    constructor() : this(0, "", null)

    companion object {
        fun of(s3PostImageUrlDto: S3PostImageUrlDto) : ImageColumn {
            return ImageColumn(imageId = s3PostImageUrlDto.imageId, s3Url = s3PostImageUrlDto.s3Url, description = s3PostImageUrlDto.description)
        }
    }
}