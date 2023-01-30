package com.wafflytime.user.info.dto

import org.hibernate.validator.constraints.Length

data class UpdateUserInfoRequest(
    val oldPassword: String?,
    val newPassword: String?,
    @field:Length(min = 2, max = 10)
    val nickname: String?,
)