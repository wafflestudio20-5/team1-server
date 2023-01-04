package com.wafflytime.board.database

import com.wafflytime.common.BaseTimeEntity
import jakarta.persistence.*


// reply와 scrap은 entity 이름을 네이밍 컨벤션에 따라 단수로 지었다. 하지만 like는 MySQL에서 사용하고 있는 예약어라 likes로 설정함
@Entity
@Table(name="likes")
class LikesEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="post_id")
    val post: PostEntity
): BaseTimeEntity()