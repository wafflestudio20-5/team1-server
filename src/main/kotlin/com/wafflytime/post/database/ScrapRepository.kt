package com.wafflytime.post.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.common.CursorPage
import com.wafflytime.post.database.QScrapEntity.scrapEntity
import com.wafflytime.user.info.database.QUserEntity.userEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import kotlin.math.min


interface ScrapRepository : JpaRepository<ScrapEntity, Long>, ScrapRepositorySupport {
    fun findByPostIdAndUserId(postId: Long, userId: Long) : ScrapEntity?
}


interface ScrapRepositorySupport {
    fun findScrapsByUserId(userId: Long, cursor: Long?, size: Long): CursorPage<ScrapEntity>
}

@Component
class ScrapRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : ScrapRepositorySupport {

    override fun findScrapsByUserId(userId: Long, cursor: Long?, size: Long): CursorPage<ScrapEntity> {
        val query = queryFactory
            .selectFrom(scrapEntity)
            .leftJoin(scrapEntity.post).fetchJoin()
            .leftJoin(userEntity).fetchJoin()
            .where(scrapEntity.user.id.eq(userId))
            .orderBy(scrapEntity.id.desc())

        val result = (cursor?.let { query.where(scrapEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()

        return CursorPage(result, result.lastOrNull()?.id, result.size.toLong())
    }

}