package com.wafflytime.board.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name="scrap")
class ScrapEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="post_id")
    val post: PostEntity
) : BaseTimeEntity()