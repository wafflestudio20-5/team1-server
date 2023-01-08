package com.wafflytime.board.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.board.database.QReplyEntity.replyEntity
import com.wafflytime.board.database.QReplyWriterEntity.replyWriterEntity
import com.wafflytime.user.info.database.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component


interface ReplyRepository : JpaRepository<ReplyEntity, Long>

@Component
class ReplyRepositorySupport(
    private val queryFactory: JPAQueryFactory,
) {
    fun countReplies(post: PostEntity): Long {
        val qPost = QPostEntity.postEntity
        return queryFactory.select(
            replyEntity.commentGroup.max()
        )
            .innerJoin(replyEntity.post, qPost)
            .fetchJoin()
            .where(qPost.id.eq(post.id))
            .fetchOne() ?: 0
    }

    fun countChildReplies(post: PostEntity, commentGroup: Long): Long {
        val qPost = QPostEntity.postEntity
        return queryFactory.select(
            replyEntity.commentOrder.max()
        )
            .innerJoin(replyEntity.post, qPost)
            .fetchJoin()
            .where(qPost.id.eq(post.id))
            .where(replyEntity.commentGroup.eq(commentGroup))
            .fetchOne() ?: 0
    }

    fun findParent(post: PostEntity, commentGroup: Long): ReplyEntity? {
        val qPost = QPostEntity.postEntity
        return queryFactory.selectFrom(replyEntity)
            .where(replyEntity.isRoot)
            .where(replyEntity.commentGroup.eq(commentGroup))
            .innerJoin(replyEntity.post, qPost)
            .fetchJoin()
            .where(qPost.id.eq(post.id))
            .fetchOne()
    }
}

@Component
interface ReplyWriterRepository : JpaRepository<ReplyWriterEntity, Long>

@Component
class ReplyWriterRepositorySupport(
    private val queryFactory: JPAQueryFactory,
) {
    fun getAnonymousId(post: PostEntity, writer: UserEntity): Long? {
        return queryFactory.selectFrom(replyWriterEntity)
            .where(replyWriterEntity.post.id.eq(post.id))
            .where(replyWriterEntity.writer.id.eq(writer.id))
            .fetchOne()
            ?.anonymousId
    }

    fun countReplyIds(post: PostEntity): Long {
        return queryFactory.select(
            replyWriterEntity.count()
        )
            .where(replyWriterEntity.post.id.eq(post.id))
            .fetchOne() ?: 0
    }
}
