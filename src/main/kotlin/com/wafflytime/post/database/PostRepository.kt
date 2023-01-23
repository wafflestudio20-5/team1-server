package com.wafflytime.post.database

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.post.database.QPostEntity.postEntity
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