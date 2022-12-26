package com.wafflytime.database

import jakarta.persistence.*

@Entity
@Table(name = "`user`")
data class UserEntity(
    @Column(name = "nickname", nullable = false)
    var nickname: String,
    @Column(name = "verifiedEmail", unique = true, nullable = true)
    var univEmail: String? = null,
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "social_email")
    var google: SocialEntity? = null,
) : BaseTimeEntity()