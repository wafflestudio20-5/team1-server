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
        return queryFactory.select(
            replyEntity.replyGroup.max()
        )
            .from(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .fetchOne() ?: 0
    }

    fun countChildReplies(post: PostEntity, commentGroup: Long): Long {
        return queryFactory.select(
            replyEntity.replyOrder.max()
        )
            .where(replyEntity.replyGroup.eq(commentGroup))
            .from(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .fetchOne() ?: 0
    }

    fun findParent(post: PostEntity, commentGroup: Long): ReplyEntity? {
        return queryFactory.selectFrom(replyEntity)
            .where(replyEntity.isRoot)
            .where(replyEntity.replyGroup.eq(commentGroup))
            .innerJoin(replyEntity.post)
            .fetchJoin()
            .where(replyEntity.post.id.eq(post.id))
            .fetchOne()
    }

    fun getReplies(post: PostEntity, page: Long, size: Long): List<ReplyEntity> {
        return queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.post)
            .fetchJoin()
            .where(replyEntity.post.id.eq(post.id))
            .orderBy(replyEntity.replyGroup.asc(), replyEntity.replyOrder.asc())
            .offset(page)
            .limit(size)
            .fetch()
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
            .innerJoin(replyWriterEntity.post)
            .fetchJoin()
            .where(replyWriterEntity.post.id.eq(post.id))
            .innerJoin(replyWriterEntity.writer)
            .fetchJoin()
            .where(replyWriterEntity.writer.id.eq(writer.id))
            .fetchOne()
            ?.anonymousId
    }

    fun countReplyIds(post: PostEntity): Long {
        return queryFactory.select(
            replyWriterEntity.count()
        )
            .from(replyWriterEntity)
            .innerJoin(replyWriterEntity.post)
            .fetchJoin()
            .where(replyWriterEntity.post.id.eq(post.id))
            .fetchOne() ?: 0
    }
}
