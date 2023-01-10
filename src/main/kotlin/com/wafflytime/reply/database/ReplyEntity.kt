package com.wafflytime.reply.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.post.database.PostEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.transaction.Transactional

@Entity
@Table(name = "reply")
class ReplyEntity(
    var contents: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wafflytime_user_id")
    val writer: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    val post: PostEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mention_id")
    val mention: ReplyEntity? = null, // 멘션한 댓글

    val replyGroup: Long, // 부모 댓글 인덱스
    val replyOrder: Long = 0, // 부모 댓글 내부에서 대댓글 순서

    val isRoot: Boolean = false, // true : 일반 댓글, false : 대댓글

    val anonymousId: Long,

    var isWriterAnonymous: Boolean = true,
    var isDisplayed: Boolean = true,
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    @Transactional
    fun update(contents: String? = null, isWriterAnonymous: Boolean? = null, isDisplayed: Boolean? = null) {
        contents?.let {
            this.contents = contents
        }
        isWriterAnonymous?.let {
            this.isWriterAnonymous = isWriterAnonymous
        }
        isDisplayed?.let {
            this.isDisplayed = isDisplayed
        }
    }

    @Transactional
    fun delete() {
        if (isDeleted) throw WafflyTime404("이미 삭제된 댓글입니다")
        isDeleted = true
    }
}