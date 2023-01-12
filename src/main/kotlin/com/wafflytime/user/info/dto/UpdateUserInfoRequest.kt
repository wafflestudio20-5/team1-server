package com.wafflytime.user.info.dto

data class UpdateUserInfoRequest(
    val password: String?,
    val nickname: String?,
)