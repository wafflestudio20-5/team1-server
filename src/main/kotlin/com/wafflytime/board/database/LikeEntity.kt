package com.wafflytime.board.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name="like")
class LikeEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="post_id")
    val post: PostEntity
): BaseTimeEntity()