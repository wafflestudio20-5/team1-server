package com.wafflytime.post.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*

@Entity
@Table(name="scrap")
class ScrapEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    val post: PostEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wafflytime_user_id")
    val user: UserEntity,
) : BaseTimeEntity()
