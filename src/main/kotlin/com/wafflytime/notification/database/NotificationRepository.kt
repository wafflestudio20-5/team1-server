package com.wafflytime.notification.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.common.CursorPage
import com.wafflytime.notification.database.QNotificationEntity.notificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface NotificationRepository : JpaRepository<NotificationEntity, Long>, NotificationRepositorySupport

interface NotificationRepositorySupport {
    fun findAllByReceiverId(userId: Long, cursor: Long?, size: Long): CursorPage<NotificationEntity>
}

@Repository
class NotificationRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory,
) : NotificationRepositorySupport {

    override fun findAllByReceiverId(userId: Long, cursor: Long?, size: Long): CursorPage<NotificationEntity> {
        val query = queryFactory
            .selectFrom(notificationEntity)
            .where(notificationEntity.receiver.id.eq(userId))
            .orderBy(notificationEntity.id.desc())

        val result = (cursor?.let { query.where(notificationEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()

        return CursorPage(result, result.lastOrNull()?.id, result.size.toLong())
    }

}