package com.wafflytime.reply.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.common.DoubleCursorPage
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

    fun getReplies(post: PostEntity, page: Long, size: Long): DoubleCursorPage<ReplyEntity> {
        val result = queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .where(replyEntity.isDisplayed.isTrue)
            .orderBy(replyEntity.replyGroup.desc(), replyEntity.id.desc())
            .offset(page * size)
            .limit(size)
            .innerJoin(replyEntity.writer)
            .fetchJoin()
            .fetch()
            .reversed()

        return DoubleCursorPage.of(
            contents = result,
            page = page,
            size = result.size.toLong(),
            requestSize = size
        )
    }

    fun getReplies(post: PostEntity, cursor: Pair<Long, Long>?, size: Long): DoubleCursorPage<ReplyEntity> {
        val query = queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.post)
            .where(replyEntity.post.id.eq(post.id))
            .where(replyEntity.isDisplayed.isTrue)
            .orderBy(replyEntity.replyGroup.desc(), replyEntity.id.desc())

        val result =
            (cursor?.run {
                query
                    .where(replyEntity.replyGroup.loe(first))
                    .where(replyEntity.replyGroup.lt(first).or(replyEntity.id.lt(second)))
            } ?: query)
                .limit(size)
                .innerJoin(replyEntity.writer)
                .fetchJoin()
                .fetch()
                .reversed()
        return DoubleCursorPage.of(
            contents = result,
            cursor = result.firstOrNull()?.run { Pair(replyGroup, id) },
            size = result.size.toLong(),
            requestSize = size
        )
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

    fun getAnonymousId(post: PostEntity, user: UserEntity): Int {
        return queryFactory.selectFrom(replyEntity)
            .innerJoin(replyEntity.writer)
            .where(replyEntity.writer.id.eq(user.id))
            .fetchFirst()
            ?.anonymousId
            ?: ++post.anonymousIds
    }
}