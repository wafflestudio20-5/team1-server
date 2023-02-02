package com.wafflytime.chat.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.chat.database.QMessageEntity.messageEntity
import com.wafflytime.common.CursorPage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

interface MessageRepository : JpaRepository<MessageEntity, Long>, MessageRepositorySupport

interface MessageRepositorySupport {
    fun findByChatIdPageable(chatId: Long, page: Long, size: Long): CursorPage<MessageEntity>
    fun findByChatIdPageable(chatId: Long, cursor: Long?, size: Long): CursorPage<MessageEntity>
}

@Repository
class MessageRepositorySupportImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(MessageEntity::class.java), MessageRepositorySupport {

    override fun findByChatIdPageable(chatId: Long, page: Long, size: Long): CursorPage<MessageEntity> {
        val result = jpaQueryFactory
            .selectFrom(messageEntity)
            .where(messageEntity.chat.id.eq(chatId))
            .orderBy(messageEntity.id.desc())
            .offset(page * size)
            .limit(size)
            .fetch()
            .reversed()

        return CursorPage.of(
            contents = result,
            page = page,
            size = result.size.toLong(),
            requestSize = size
        )
    }

    override fun findByChatIdPageable(chatId: Long, cursor: Long?, size: Long): CursorPage<MessageEntity> {
        val query = jpaQueryFactory
            .selectFrom(messageEntity)
            .where(messageEntity.chat.id.eq(chatId))
            .orderBy(messageEntity.id.desc())

        val result = (cursor?.let { query.where(messageEntity.id.lt(it)) } ?: query)
            .limit(size)
            .fetch()
            .reversed()

        return CursorPage.of(
            contents = result,
            cursor = result.firstOrNull()?.id,
            size = result.size.toLong(),
            requestSize = size
        )
    }

}