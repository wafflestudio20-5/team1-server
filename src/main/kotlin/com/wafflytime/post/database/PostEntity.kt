package com.wafflytime.post.database

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.database.image.JpaImageColumnJsonConverter
import com.wafflytime.post.dto.UpdatePostRequest
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*


@Entity
@Table(name="post")
class PostEntity(
    var title: String? = null,
    var contents: String,

    @Column(length = 5000)
    @Convert(converter = JpaImageColumnJsonConverter::class)
    var images: Map<String, ImageColumn>? = null,

    // writer 와 board 의 owner 인 유저만 post 를 지울 수 있음
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val writer: UserEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="board_id")
    val board: BoardEntity,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val scraps: MutableList<ScrapEntity> = mutableListOf(),

    // TODO : 질문글인 경우 게시판 상위로 보여지게 하는 알고리즘도 적용하면 좋겠지만 시간이 되면 도전
    var isQuestion: Boolean = false,
    var isWriterAnonymous: Boolean = true,

    var replies: Long = 0, // 전체 댓글 개수
    var anonymousIds: Long = 0, // 익명 댓글 개수

    var nLikes: Int = 0

) : BaseTimeEntity() {

    fun update(request: UpdatePostRequest) {
        this.title = request.title ?: this.title
        this.contents = request.contents ?: this.contents
        this.isQuestion = request.isQuestion ?: this.isQuestion
        this.isWriterAnonymous = request.isWriterAnonymous ?: this.isWriterAnonymous
    }

    fun update(request: UpdatePostRequest, imageColumnList: Map<String, ImageColumn>?) {
        this.title = request.title ?: this.title
        this.contents = request.contents ?: this.contents
        this.isQuestion = request.isQuestion ?: this.isQuestion
        this.isWriterAnonymous = request.isWriterAnonymous ?: this.isWriterAnonymous
        this.images = imageColumnList
    }
}