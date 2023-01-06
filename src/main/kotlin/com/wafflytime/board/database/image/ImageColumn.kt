package com.wafflytime.board.database.image

import com.wafflytime.board.dto.S3ImageUrlDto

data class ImageColumn(
    val s3Urls: String,
    val description: String?
) {
    // 반드시 default 부생성자가 필요함 - 지우면 안됨
    constructor() : this("", null)

    companion object {
        fun of(s3ImageUrlDto: S3ImageUrlDto) : ImageColumn {
            return ImageColumn(s3Urls = s3ImageUrlDto.s3Url, description = s3ImageUrlDto.description)
        }
    }
}