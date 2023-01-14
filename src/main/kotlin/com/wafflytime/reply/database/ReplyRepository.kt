package com.wafflytime.reply.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.post.database.PostEntity
import com.wafflytime.reply.database.QReplyEntity.replyEntity
import com.wafflytime.user.info.database.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface ReplyRepository : JpaRepository<ReplyEntity, Long>

@Component
class ReplyRepositorySupport(
    private val queryFactory: JPAQueryFactory,
) {
    fun getLastReplyGroup(post: PostEntity): Long {
        return queryFactory.select(
            replyEntity.replyGroup.max()
        )
            .from(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .fetchOne() ?: 0
    }

    fun findParent(post: PostEntity, replyGroup: Long): ReplyEntity? {
        return queryFactory.selectFrom(replyEntity)
            .where(replyEntity.isRoot)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .where(replyEntity.replyGroup.eq(replyGroup))
            .fetchOne()
    }

    fun getReplies(post: PostEntity, page: Long, size: Long): List<ReplyEntity> {
        return queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .where(replyEntity.isDisplayed.isTrue)
            .orderBy(replyEntity.replyGroup.desc(), replyEntity.createdAt.desc())
            .offset(page * size)
            .limit(size)
            .fetch()
            .reversed()
    }

    fun countChildReplies(post: PostEntity, replyGroup: Long): Long {
        return queryFactory.select(
            replyEntity.count()
        )
            .from(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .where(replyEntity.replyGroup.eq(replyGroup))
            .where(replyEntity.isDeleted.isFalse)
            .fetchOne() ?: 0
    }

    fun getAnonymousId(post: PostEntity, user: UserEntity): Long {
        return queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.writer)
            .where(replyEntity.writer.id.eq(user.id))
            .fetchFirst()
            ?.anonymousId
            ?: ++post.anonymousIds
    }
}