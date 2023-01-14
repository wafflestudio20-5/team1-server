package com.wafflytime.post.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table


@Entity
@Table(name="post_like")
class PostLikeEntity(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    val post: PostEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wafflytime_user_id")
    val user: UserEntity,

) : BaseTimeEntity() {

}