package com.wafflytime.reply.database

import com.wafflytime.post.database.PostEntity
import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*

@Entity
@Table(name = "reply_writer")
data class ReplyWriterEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    val post: PostEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="wafflytime_user_id")
    val writer: UserEntity,

    val anonymousId: Long,
) : BaseTimeEntity()