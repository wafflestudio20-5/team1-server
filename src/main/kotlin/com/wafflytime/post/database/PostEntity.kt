package com.wafflytime.post.database

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.database.image.JpaImageColumnJsonConverter
import com.wafflytime.post.dto.UpdatePostRequest
import com.wafflytime.reply.database.ReplyEntity
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


    // 서비스 로직에서 post의 scraps를 불러오거나 likes를 불러올 일이 없지만,
    // post가 삭제될 때 연관된 모든 scrap과 like가 자동으로 jpa에서 삭제 될 수 있게 OneToMany로 설정해두었다
    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val scraps: MutableList<ScrapEntity> = mutableListOf(),
    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val likes: MutableList<PostLikeEntity> = mutableListOf(),

    // nLikes와 nScraps를 넣어둔 이유는 post를 불러올 때마다 scraps나 likes가 궁금한 것이 아니라 개수가 궁금한 것인데
    // 개수를 알기 위해 join 하는 것은 비효율적이기 때문에 개수를 column에 추가
    var nLikes: Int = 0,
    var nScraps: Int = 0,

    // TODO : 질문글인 경우 게시판 상위로 보여지게 하는 알고리즘도 적용하면 좋겠지만 시간이 되면 도전
    var isQuestion: Boolean = false,
    var isWriterAnonymous: Boolean = true,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val replies: MutableList<ReplyEntity> = mutableListOf(),

    var nReplies: Int = 0, // 전체 댓글 개수
    var anonymousIds: Int = 0, // 익명 댓글 개수


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