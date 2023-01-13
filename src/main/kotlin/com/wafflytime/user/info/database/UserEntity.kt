package com.wafflytime.user.info.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name="wafflytime_user")
class UserEntity(
    @field:Column(unique = true)
    val loginId: String? = null,
    var password: String? = null,
    @field:Column(unique = true)
    val socialEmail: String? = null,
    @field:Column(unique = true)
    var univEmail: String? = null,
    var nickname: String? = null,
    val isAdmin: Boolean = false,

    ): BaseTimeEntity() {

    fun update(password: String?, nickname: String?) {
        if (loginId != null && password != null) this.password = password
        if (nickname != null) this.nickname = nickname
    }

    /**
     * TODO:
     *  작성글 목록
     *  좋아요 목록
     *  스크랩 목록
     *  쪽지 관련
     *  etc
     */
}