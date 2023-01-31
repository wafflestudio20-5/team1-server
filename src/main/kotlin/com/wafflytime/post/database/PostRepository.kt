package com.wafflytime.post.database

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.board.database.QBoardEntity.boardEntity
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.common.CursorPage
import com.wafflytime.common.DoubleCursorPage
import com.wafflytime.post.database.QPostEntity.postEntity
import kotlinx.coroutines.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface PostRepository : JpaRepository<PostEntity, Long>, PostRepositorySupport

interface PostRepositorySupport {
    fun findAllByBoardId(boardId: Long, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun findAllByWriterId(writerId: Long, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun getHotPosts(cursor: Long?, size: Long): CursorPage<PostEntity>
    fun getBestPosts(cursor: Pair<Long, Long>?, size: Long): DoubleCursorPage<PostEntity>
    fun findPostsByKeyword(keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity>
    fun findHomePostsByQuery() : List<PostEntity>
    fun findLatestPostsByCategory(category: BoardCategory, size: Int): List<PostEntity>
    fun findLatestPostsByBoardId(boardId: Long, limit: Long) : List<PostEntity>
}

@Component
class PostRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositorySupport {

    private val hotPostMinLikes = 10
    private val bestPostMinLikes = 20

    override fun findAllByBoardId(boardId: Long, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.board.id.eq(boardId), cursor, size)
    }

    override fun findAllByWriterId(writerId: Long, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.writer.id.eq(writerId), cursor, size)
    }

    override fun getHotPosts(cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.nLikes.goe(hotPostMinLikes), cursor, size)
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

        return DoubleCursorPage(result, result.lastOrNull()?.run { Pair(nLikes.toLong(), id) }, result.size.toLong())
    }

    override fun findPostsByKeyword(keyword: String, cursor: Long?, size: Long): CursorPage<PostEntity> {
        return findAllByConditionDesc(postEntity.contents.contains(keyword), cursor, size)
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

    private fun findAllByConditionDesc(whereCondition: BooleanExpression, cursor: Long?, size: Long) : CursorPage<PostEntity> {
        val query = queryFactory
            .selectFrom(postEntity)
            .where(whereCondition)
            .orderBy(postEntity.id.desc())

        val result = (cursor?.let { query.where(postEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()

        return CursorPage(result, result.lastOrNull()?.id, result.size.toLong())
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