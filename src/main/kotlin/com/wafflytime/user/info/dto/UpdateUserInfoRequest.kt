package com.wafflytime.user.info.dto

data class UpdateUserInfoRequest(
    val oldPassword: String?,
    val newPassword: String?,
    val nickname: String?,
)