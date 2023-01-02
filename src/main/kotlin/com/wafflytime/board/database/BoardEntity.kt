package com.wafflytime.board.database

import com.wafflytime.board.types.BoardType
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

    @field:NotNull @Enumerated(EnumType.STRING) val type: BoardType,

    // TODO: 에타에서 게시판을 생성해 본 적이 없어서 게시판 생성을 요청한 학생에게 게시판 삭제 권한도 주어지는 것인지 확인. 임의로 우리가 하고 싶은대로 해도 될 듯
    // Owner null인 경우는 학생이 직접 생성한 게시판이 아닌 default로 만들어놓은 게시판(ex 자유게시판)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val user: UserEntity? = null,

    @OneToMany(mappedBy = "board", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val posts: MutableList<PostEntity> = mutableListOf()

    // TODO: 시간이 남는다면 홍보성게시판인지, 새내기게시판인지, 특별한 제한이 없는 게시판인지도 구분해서 저장하면 좋을 듯 하다
) : BaseTimeEntity()