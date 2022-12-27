package com.wafflytime.database

import jakarta.persistence.*

@Entity
data class SocialEntity(
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val user: UserEntity? = null,
    val provider: String,
    val email: String,
) : BaseTimeEntity()