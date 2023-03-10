package com.wafflytime.post.database

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.board.database.QBoardEntity.boardEntity
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.common.CursorPage
import com.wafflytime.common.DoubleCursorPage
import com.wafflytime.post.database.QPostEntity.postEntity
import com.wafflytime.reply.database.QReplyEntity.replyEntity
import kotlinx.coroutines.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface PostRepository : JpaRepository<PostEntity, Long>, PostRepositorySupport

interface PostRepositorySupport {
    fun findAllByBoardId(boardId: Long, page: Long, size: Long): CursorPage<PostEntity>
    fun findAllByBoardId(boardId: Long, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun findAllByWriterId(writerId: Long, page: Long, size: Long): CursorPage<PostEntity>
    fun findAllByWriterId(writerId: Long, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun getHotPosts(page: Long, size: Long): CursorPage<PostEntity>
    fun getHotPosts(cursor: Long?, size: Long): CursorPage<PostEntity>
    fun getBestPosts(page: Long, size: Long): DoubleCursorPage<PostEntity>
    fun getBestPosts(cursor: Pair<Long, Long>?, size: Long): DoubleCursorPage<PostEntity>
    fun findPostsByKeyword(keyword: String, page: Long, size: Long): CursorPage<PostEntity>
    fun findPostsByKeyword(keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun findPostsInBoardByKeyword(boardId: Long, keyword: String, page: Long, size: Long): CursorPage<PostEntity>
    fun findPostsInBoardByKeyword(boardId: Long, keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun findHomePostsByQuery() : List<PostEntity>
    fun findLatestPostsByCategory(category: BoardCategory, size: Int): List<PostEntity>
    fun findLatestPostsByBoardId(boardId: Long, limit: Long) : List<PostEntity>
    fun findAllByUserReply(userId: Long, page: Long, size: Long): CursorPage<PostEntity>
    fun findAllByUserReply(userId: Long, cursor: Long?, size: Long): CursorPage<PostEntity>
}

@Component
class PostRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositorySupport {

    private val hotPostMinLikes = 10
    private val bestPostMinLikes = 20

    override fun findAllByBoardId(boardId: Long, page: Long, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.board.id.eq(boardId), page, size)
    }

    override fun findAllByBoardId(boardId: Long, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.board.id.eq(boardId), cursor, size)
    }

    override fun findAllByWriterId(writerId: Long, page: Long, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.writer.id.eq(writerId), page, size)
    }

    override fun findAllByWriterId(writerId: Long, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.writer.id.eq(writerId), cursor, size)
    }

    override fun getHotPosts(page: Long, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.nLikes.goe(hotPostMinLikes), page, size)
    }

    override fun getHotPosts(cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.nLikes.goe(hotPostMinLikes), cursor, size)
    }

    override fun getBestPosts(page: Long, size: Long): DoubleCursorPage<PostEntity> {
        val result = queryFactory
            .selectFrom(postEntity)
            .where(postEntity.nLikes.goe(bestPostMinLikes))
            .orderBy(postEntity.nLikes.desc(), postEntity.id.desc())
            .offset(page * size)
            .limit(size)
            .fetch()

        return DoubleCursorPage.of(
            contents = result,
            page = page,
            size = result.size.toLong(),
            requestSize = size
        )
    }

    override fun getBestPosts(cursor: Pair<Long, Long>?, size: Long): DoubleCursorPage<PostEntity> {
        val query = queryFactory
            .selectFrom(postEntity)
            .where(postEntity.nLikes.goe(bestPostMinLikes))
            .orderBy(postEntity.nLikes.desc(), postEntity.id.desc())

        val result =
            (cursor?.run {
                query
                    .where(postEntity.nLikes.loe(first.toInt()))
                    .where(postEntity.nLikes.lt(first.toInt()).or(postEntity.id.lt(second)))
            } ?: query)
                .limit(size)
                .fetch()

        return DoubleCursorPage.of(
            contents = result,
            cursor = result.lastOrNull()?.run { Pair(nLikes.toLong(), id) },
            size = result.size.toLong(),
            requestSize = size
        )
    }

    override fun findPostsByKeyword(keyword: String, page: Long, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.contents.contains(keyword).or(postEntity.title.contains(keyword)), page, size)
    }

    override fun findPostsByKeyword(keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.contents.contains(keyword).or(postEntity.title.contains(keyword)), cursor, size)
    }

    override fun findPostsInBoardByKeyword(boardId: Long, keyword: String, page: Long, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.board.id.eq(boardId).and(postEntity.contents.contains(keyword).or(postEntity.title.contains(keyword))), page, size)
    }

    override fun findPostsInBoardByKeyword(boardId: Long, keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.board.id.eq(boardId).and(postEntity.contents.contains(keyword).or(postEntity.title.contains(keyword))), cursor, size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun findHomePostsByQuery(): List<PostEntity> {
        val boards = queryFactory.select(boardEntity)
            .from(boardEntity)
            .where(boardEntity.category.`in`(BoardCategory.BASIC, BoardCategory.CAREER))
            .fetch()

        val future = mutableListOf<Deferred<List<PostEntity>>>()

        boards.forEach {
            future.add(CoroutineScope(Dispatchers.Default).async {
                findLatestPostsByBoardId(
                    boardId = it.id,
                    limit = if (it.type.name.startsWith("CUSTOM")) 2 else 4)
            })
        }
        runBlocking { future.forEach { it.await() } }
        return future.flatMap { it.getCompleted().reversed() }
    }

    override fun findLatestPostsByCategory(category: BoardCategory, size: Int): List<PostEntity> {
        return findLatestPosts(boardEntity.category.eq(category), size.toLong())
    }

    override fun findLatestPostsByBoardId(boardId: Long, limit: Long) : List<PostEntity> {
        return findLatestPosts(boardEntity.id.eq(boardId), limit)
    }

    override fun findAllByUserReply(userId: Long, page: Long, size: Long): CursorPage<PostEntity> {
        val query = queryFactory.select(postEntity)
            .from(replyEntity)
            .leftJoin(postEntity).on(replyEntity.post.id.eq(postEntity.id))
            .where(replyEntity.writer.id.eq(userId))
            .orderBy(postEntity.createdAt.desc())
            .groupBy(postEntity.id)

        return getCursorPagedPostsByPostId(query, page, size)
    }

    override fun findAllByUserReply(userId: Long, cursor: Long?, size: Long): CursorPage<PostEntity> {
        val query = queryFactory.select(postEntity)
            .from(replyEntity)
            .leftJoin(postEntity).on(replyEntity.post.id.eq(postEntity.id))
            .where(replyEntity.writer.id.eq(userId))
            .orderBy(postEntity.createdAt.desc())
            .groupBy(postEntity.id)

        return getCursorPagedPostsByPostId(query, cursor, size)
    }

    private fun findAllByConditionDesc(whereCondition: BooleanExpression, page: Long, size: Long) : CursorPage<PostEntity> {
        val query = queryFactory
            .selectFrom(postEntity)
            .where(whereCondition)
            .orderBy(postEntity.id.desc())

        return getCursorPagedPostsByPostId(query, page, size)
    }

    private fun findAllByConditionDesc(whereCondition: BooleanExpression, cursor: Long?, size: Long) : CursorPage<PostEntity> {
        val query = queryFactory
            .selectFrom(postEntity)
            .where(whereCondition)
            .orderBy(postEntity.id.desc())

        return getCursorPagedPostsByPostId(query, cursor, size)
    }

    private fun getCursorPagedPostsByPostId(query: JPAQuery<PostEntity>, page: Long, size: Long) : CursorPage<PostEntity> {
        val result = query
            .offset(page * size)
            .limit(size)
            .fetch()

        return CursorPage.of(
            contents = result,
            page = page,
            size = result.size.toLong(),
            requestSize = size
        )
    }

    private fun getCursorPagedPostsByPostId(query: JPAQuery<PostEntity>, cursor: Long?, size: Long) : CursorPage<PostEntity> {
        val result = (cursor?.let { query.where(postEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()
        return CursorPage.of(
            contents = result,
            cursor = result.lastOrNull()?.id,
            size = result.size.toLong(),
            requestSize = size
        )
    }

    private fun findLatestPosts(whereCondition: BooleanExpression, limit: Long) : List<PostEntity> {
        return queryFactory.selectFrom(postEntity)
            .leftJoin(boardEntity).on(postEntity.board.id.eq(boardEntity.id))
            .where(whereCondition)
            .orderBy(postEntity.createdAt.desc())
            .limit(limit)
            .fetch()
    }

}