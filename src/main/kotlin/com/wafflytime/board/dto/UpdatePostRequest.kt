package com.wafflytime.board.dto

data class UpdatePostRequest(
    val title: String?,
    val contents: String?,
    val isQuestion: Boolean?,
    val isWriterAnonymous: Boolean?,

    // 이 필드는 반드시 프론트에서 현재 상태를 담아서 보내줘야 한다.
    // 이 필드가 비어져 있다고 해서 "변화 없음" 을 의미하는 것이 아니라 유저가 사진을 하나도 올리지 않았음을 의미한다
    val images: List<ImageRequest>?,
    val deletedFileNames: List<String>?
)
