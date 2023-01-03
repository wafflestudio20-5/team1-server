package com.wafflytime.user.info.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.type.UserRole
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

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

    @field:NotNull @Enumerated(EnumType.STRING)
    val role: UserRole
): BaseTimeEntity() {
    /**
     * TODO:
     *  작성글 목록
     *  좋아요 목록
     *  스크랩 목록
     *  쪽지 관련
     *  etc
     */
}