package com.wafflytime.database

import jakarta.persistence.Entity

@Entity
class UserEntity(
    val loginId: String? = null,
    var password: String? = null,
    val socialEmail: String? = null,
    var univEmail: String? = null,
    var nickname: String? = null,
): BaseTimeEntity() {

}