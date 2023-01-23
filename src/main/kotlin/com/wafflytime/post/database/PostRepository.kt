package com.wafflytime.post.database

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.board.database.QBoardEntity.boardEntity
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.post.database.QPostEntity.postEntity
import com.wafflytime.post.dto.HomePostResponse
import com.wafflytime.post.dto.PostResponse
import kotlinx.coroutines.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface PostRepository : JpaRepository<PostEntity, Long>, PostRepositorySupport {
    fun findAllByBoardId(boardId: Long, pageable: Pageable) : Page<PostEntity>
    fun findAllByWriterId(writerId: Long, pageable: Pageable): Page<PostEntity>

}

interface PostRepositorySupport {
    fun getHotPosts(pageable: Pageable): Page<PostEntity>
    fun getBestPosts(pageable: Pageable): Page<PostEntity>
    fun findPostsByKeyword(keyword: String, pageable: Pageable): Page<PostEntity>
    fun findHomePostsGrouped() : List<HomePostResponse>
}

@Component
class PostRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : PostRepositorySupport {

    private val hotPostMinLikes = 10
    private val bestPostMinLikes = 20

    override fun getHotPosts(pageable: Pageable): Page<PostEntity> {
        return getPostsOnLikesQuery(pageable, hotPostMinLikes, postEntity.createdAt.desc())
    }

    override fun getBestPosts(pageable: Pageable): Page<PostEntity> {
        return getPostsOnLikesQuery(pageable, bestPostMinLikes, postEntity.nLikes.desc())
    }

    override fun findPostsByKeyword(keyword: String, pageable: Pageable): Page<PostEntity> {
        val result = queryFactory
            .selectFrom(postEntity)
            .where(postEntity.contents.contains(keyword))
            .orderBy(postEntity.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        return PageImpl(result, pageable, result.size.toLong())
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun findHomePostsGrouped(): List<HomePostResponse> {
        val boards = queryFactory.select(boardEntity)
            .from(boardEntity)
            .where(boardEntity.category.`in`(BoardCategory.BASIC, BoardCategory.CAREER))
            .fetch()

        val future = mutableListOf<Deferred<List<PostEntity>>>()

        boards.forEach {
            future.add(CoroutineScope(Dispatchers.Default).async {
                findLatestPosts(
                    boardId = it.id,
                    limit = if (it.type.name.startsWith("CUSTOM")) 2 else 4)
            })
        }
        runBlocking { future.forEach { it.await() } }

        return boards.mapIndexed { index, boardEntity
            ->
            HomePostResponse.of(
                boardEntity,
                future[index].getCompleted().map { PostResponse.of(it) }
            ) }
    }


    private fun findLatestPosts(boardId: Long, limit: Long) : List<PostEntity> {
        return queryFactory.selectFrom(postEntity)
            .leftJoin(boardEntity).on(postEntity.board.id.eq(boardEntity.id))
            .where(boardEntity.id.eq(boardId))
            .orderBy(postEntity.createdAt.desc())
            .limit(limit)
            .fetch()
    }

    private fun getPostsOnLikesQuery(
        pageable: Pageable,
        minLikes: Int,
        order: OrderSpecifier<*>
    ) : Page<PostEntity> {
        val result = queryFactory
            .selectFrom(postEntity)
            .where(postEntity.nLikes.goe(minLikes))
            .orderBy(order)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        return PageImpl(result, pageable, result.size.toLong())
    }
}