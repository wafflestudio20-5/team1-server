package com.wafflytime.board.database

import com.wafflytime.board.type.BoardType
import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Entity
@Table(name="board")
class BoardEntity(
    @field:NotBlank
    @field:Column(unique = true)
    val title: String,
    val description: String = "",

    @field:NotNull @Enumerated(EnumType.STRING) val type: BoardType,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val user: UserEntity? = null,

    @OneToMany(mappedBy = "board", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val posts: MutableList<PostEntity> = mutableListOf(),
    val allowAnonymous: Boolean

    // TODO: 시간이 남는다면 홍보성게시판인지, 새내기게시판인지, 특별한 제한이 없는 게시판인지도 구분해서 저장하면 좋을 듯 하다
) : BaseTimeEntity()