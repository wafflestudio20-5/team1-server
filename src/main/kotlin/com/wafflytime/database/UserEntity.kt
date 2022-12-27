package com.wafflytime.database

import jakarta.persistence.*

@Entity
data class UserEntity(
    var nickname: String,
    var univEmail: String? = null,
) : BaseTimeEntity()