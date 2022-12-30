package com.wafflytime.database

import jakarta.persistence.*

@Entity
data class UserEntity(
    var nickname: String,
    var email: String,
    var password: String? = null,
    var socialEmail: String? = null,
    var univEmail: String? = null,
) : BaseTimeEntity()