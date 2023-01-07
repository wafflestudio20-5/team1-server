package com.wafflytime.board.dto


data class ImageRequest (
    val imageId: Int,
    val fileName: String,
    val description: String?
)


data class ImageResponse(
    val imageId: Int,
    val preSignedUrl: String?,
    val description: String?
) {
    companion object {
        fun of(s3ImageUrlDto: S3ImageUrlDto) : ImageResponse {
            return ImageResponse(s3ImageUrlDto.imageId, s3ImageUrlDto.preSignedUrl, s3ImageUrlDto.description)
        }
    }
}

data class S3ImageUrlDto(
    val imageId: Int,
    val fileName: String,
    val s3Url: String,
    val preSignedUrl: String?,
    val description: String?
)