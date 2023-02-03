package com.wafflytime.post.dto

data class ImageRequest (
    val imageId: Int,
    val fileName: String,
    val description: String?
)


data class ImageResponse(
    val imageId: Int,
    val filename: String,
    val preSignedUrl: String?,
    val description: String?
) {
    companion object {
        fun of(s3PostImageUrlDto: S3PostImageUrlDto) : ImageResponse {
            return ImageResponse(s3PostImageUrlDto.imageId, s3PostImageUrlDto.fileName, s3PostImageUrlDto.preSignedUrl, s3PostImageUrlDto.description)
        }
    }
}

data class S3PostImageUrlDto(
    val imageId: Int,
    val fileName: String,
    val s3Url: String,
    val preSignedUrl: String?,
    val description: String?
)

data class S3ImageUrlDto(
    val s3Url: String,
    val preSignedUrl: String?
)