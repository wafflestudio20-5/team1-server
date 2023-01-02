package com.wafflytime.board.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import org.springframework.boot.autoconfigure.security.SecurityProperties.User

@Entity
@Table(name="post")
class PostEntity(
    val title: String,

    // writer 와 board 의 owner 인 유저만 post 를 지울 수 있음
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val writer: UserEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="board_id")
    val board: BoardEntity,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val replies: MutableList<ReplyEntity> = mutableListOf(),
//
//    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
//    val likes: MutableList<LikeEntity> = mutableListOf(),
//
//    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
//    val scraps: MutableList<ScrapEntity> = mutableListOf(),

    // 질문글인 경우 게시판 상위로 보여지게 하는 알고리즘도 적용하면 좋겠지만 시간이 안 될 것 같다
    val isQuestion: Boolean = false
) : BaseTimeEntity()