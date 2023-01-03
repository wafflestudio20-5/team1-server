package com.wafflytime.user.info.api.dto

data class UpdateUserInfoRequest(
    val password: String?,
    val nickname: String?,
)