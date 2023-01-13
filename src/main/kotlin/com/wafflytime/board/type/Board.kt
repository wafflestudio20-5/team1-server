package com.wafflytime.board.type

enum class BoardType {
    DEFAULT, CUSTOM_BASE, CUSTOM_PHOTO
}

enum class BoardCategory (val defaultDisplayColumnSize: Int) {
    BASIC(2),
    CAREER(1),
    STUDENT(1),
    DEPARTMENT(1),
    OTHER(2)
}