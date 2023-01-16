package com.wafflytime.chat.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.chat.database.QMessageEntity.messageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

interface MessageRepository : JpaRepository<MessageEntity, Long>, MessageRepositorySupport

interface MessageRepositorySupport {
    fun findByChatIdPageable(chatId: Long, pageable: Pageable): Page<MessageEntity>
}

@Repository
class MessageRepositorySupportImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(MessageEntity::class.java), MessageRepositorySupport {

    override fun findByChatIdPageable(chatId: Long, pageable: Pageable): Page<MessageEntity> {
        val query = jpaQueryFactory
            .selectFrom(messageEntity)
            .where(messageEntity.chat.id.eq(chatId))

        val count = query.fetch().size.toLong()
        val result = query
            .orderBy(messageEntity.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
            .reversed()

        return PageImpl(result, pageable, count)
    }

}