package com.wafflytime.board.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name="reply")
class ReplyEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="post_id")
    val post: PostEntity
): BaseTimeEntity()