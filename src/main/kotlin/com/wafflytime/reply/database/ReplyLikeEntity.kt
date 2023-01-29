package com.wafflytime.reply.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "reply_like")
class ReplyLikeEntity(
    @ManyToOne
    @JoinColumn(name = "reply_id")
    val reply: ReplyEntity,
    @ManyToOne
    @JoinColumn(name = "wafflytime_user_id")
    val user: UserEntity,
) : BaseTimeEntity()