package com.wafflytime.database

import jakarta.persistence.*

@Entity
@Table(name = "social")
data class SocialEntity(
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity? = null,
    @Column(name = "provider")
    val provider: String,
    @Column(name = "email")
    val email: String,
) : BaseTimeEntity()