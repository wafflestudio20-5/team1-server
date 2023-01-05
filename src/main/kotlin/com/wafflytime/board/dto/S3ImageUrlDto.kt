package com.wafflytime.board.dto

data class S3ImageUrlDto(
    val s3Urls: MutableList<String> = mutableListOf(),
    val preSignedUrls: MutableList<String> = mutableListOf()
)
