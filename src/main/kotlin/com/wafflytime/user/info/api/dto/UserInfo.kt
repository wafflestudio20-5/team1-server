package com.wafflytime.user.info.api.dto

import com.wafflytime.user.info.database.UserEntity

data class UserInfo(
    val loginId: String?,
    val socialEmail: String?,
    val univEmail: String?,
    val nickname: String?,
) {

    companion object {
        fun of(entity: UserEntity): UserInfo = entity.run {
            UserInfo(
                loginId,
                socialEmail,
                univEmail,
                nickname,
            )
        }
    }

}