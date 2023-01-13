package com.wafflytime.post.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.post.database.QScrapEntity.scrapEntity
import com.wafflytime.user.info.database.QUserEntity.userEntity
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import kotlin.math.min


interface ScrapRepository : JpaRepository<ScrapEntity, Long>, ScrapRepositorySupport {
    fun findByPostIdAndUserId(postId: Long, userId: Long) : ScrapEntity?
}


interface ScrapRepositorySupport {
    fun findScrapsByUserId(userId: Long, pageable: Pageable) : List<ScrapEntity>
}

@Component
class ScrapRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : ScrapRepositorySupport {
    override fun findScrapsByUserId(userId: Long, pageable: Pageable): List<ScrapEntity> {
        // N+1 문제를 해결하고자 fetchJoin() 쓰고, 대신에 pageable 은 마지막에 따로 처리
        val result = queryFactory
            .selectFrom(scrapEntity)
            .leftJoin(scrapEntity.post).fetchJoin()
            .leftJoin(userEntity).fetchJoin()
            .where(scrapEntity.user.id.eq(userId))
            .orderBy(scrapEntity.createdAt.desc())
            .fetch()
        return createPageableResult(result, pageable)
    }

    fun <T> createPageableResult(result: MutableList<T>, pageable: Pageable) : List<T> {
        var start = pageable.offset.toInt()
        val end = min(start + pageable.pageSize, result.size)

        if (start > end) {
            start = end
        }
        return PageImpl(result.subList(start, end), pageable, result.size.toLong()).content
    }
}