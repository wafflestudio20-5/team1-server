package com.wafflytime.notification.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.common.CursorPage
import com.wafflytime.notification.database.QNotificationEntity.notificationEntity
import com.wafflytime.notification.type.NotificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface NotificationRepository : JpaRepository<NotificationEntity, Long>, NotificationRepositorySupport

interface NotificationRepositorySupport {
    fun findAllByReceiverId(userId: Long, page: Long, size: Long): CursorPage<NotificationEntity>
    fun findAllByReceiverId(userId: Long, cursor: Long?, size: Long): CursorPage<NotificationEntity>
}

@Repository
class NotificationRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory,
) : NotificationRepositorySupport {

    override fun findAllByReceiverId(userId: Long, page: Long, size: Long): CursorPage<NotificationEntity> {
        val result = queryFactory
            .selectFrom(notificationEntity)
            .where(!notificationEntity.notificationType.eq(NotificationType.MESSAGE))
            .where(notificationEntity.receiver.id.eq(userId))
            .orderBy(notificationEntity.id.desc())
            .offset(page * size)
            .limit(size)
            .fetch()

        return CursorPage(
            contents = result,
            page = page,
            size = result.size.toLong()
        )
    }

    override fun findAllByReceiverId(userId: Long, cursor: Long?, size: Long): CursorPage<NotificationEntity> {
        val query = queryFactory
            .selectFrom(notificationEntity)
            .where(!notificationEntity.notificationType.eq(NotificationType.MESSAGE))
            .where(notificationEntity.receiver.id.eq(userId))
            .orderBy(notificationEntity.id.desc())

        val result = (cursor?.let { query.where(notificationEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()

        return CursorPage(
            contents = result,
            cursor = result.lastOrNull()?.id,
            size = result.size.toLong()
        )
    }

}