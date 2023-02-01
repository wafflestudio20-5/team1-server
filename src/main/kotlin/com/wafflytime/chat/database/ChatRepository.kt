package com.wafflytime.chat.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.chat.database.QChatEntity.chatEntity
import com.wafflytime.chat.database.QMessageEntity.messageEntity
import com.wafflytime.chat.exception.ChatNotFound
import com.wafflytime.common.CursorPage
import com.wafflytime.user.info.database.QUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

interface ChatRepository : JpaRepository<ChatEntity, Long>, ChatRepositorySupport

interface ChatRepositorySupport {
    fun findByIdWithLastMessage(chatId: Long): ChatEntity?
    fun findAllByParticipantIdWithLastMessage(userId: Long, cursor: Long?, size: Long): CursorPage<ChatEntity>
    fun findByAllConditions(postId: Long, participantId1: Long, isAnonymous1: Boolean, participantId2: Long, isAnonymous2: Boolean) : ChatEntity?
    fun findByBothParticipantId(participantId1: Long, participantId2: Long): ChatEntity?
    fun findAllByParticipantId(participantId: Long): List<ChatEntity>
}

@Repository
class ChatRepositorySupportImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(ChatEntity::class.java), ChatRepositorySupport {

    override fun findByIdWithLastMessage(chatId: Long): ChatEntity? {
        val userEntity1 = QUserEntity("userEntity1")
        val userEntity2 = QUserEntity("userEntity2")

        return jpaQueryFactory
            .selectFrom(chatEntity)
            .where(chatEntity.id.eq(chatId))
            .leftJoin(chatEntity.messages, messageEntity)
            .where(messageEntity.chat.id.eq(chatEntity.id))
            .fetchJoin()
            .leftJoin(chatEntity.participant1, userEntity1)
            .where(userEntity1.id.eq(chatEntity.participant1.id))
            .fetchJoin()
            .leftJoin(chatEntity.participant2, userEntity2)
            .where(userEntity2.id.eq(chatEntity.participant2.id))
            .fetchJoin()
            .fetchOne()
    }

    override fun findAllByParticipantIdWithLastMessage(userId: Long, cursor: Long?, size: Long): CursorPage<ChatEntity> {
        val userEntity1 = QUserEntity("userEntity1")
        val userEntity2 = QUserEntity("userEntity2")

        val cursorEntity = cursor?.let {
            jpaQueryFactory
                .selectFrom(chatEntity)
                .where(chatEntity.id.eq(it))
                .fetchOne()
                ?: throw ChatNotFound
        }

        val query = jpaQueryFactory
            .selectFrom(chatEntity)
            .where(chatEntity.participant1.id.eq(userId).or(chatEntity.participant2.id.eq(userId)))
            .orderBy(chatEntity.modifiedAt.desc())

        val result = (cursorEntity?.let { query.where(chatEntity.modifiedAt.lt(it.modifiedAt)) } ?: query)
            .limit(size)
            .leftJoin(chatEntity.messages, messageEntity)
            .where(messageEntity.chat.id.eq(chatEntity.id))
            .fetchJoin()
            .leftJoin(chatEntity.participant1, userEntity1)
            .where(userEntity1.id.eq(chatEntity.participant1.id))
            .fetchJoin()
            .leftJoin(chatEntity.participant2, userEntity2)
            .where(userEntity2.id.eq(chatEntity.participant2.id))
            .fetchJoin()
            .fetch()

        return CursorPage(result, result.lastOrNull()?.id, result.size.toLong())
    }

    override fun findByAllConditions(
        postId: Long,
        participantId1: Long,
        isAnonymous1: Boolean,
        participantId2: Long,
        isAnonymous2: Boolean
    ): ChatEntity? {
        return jpaQueryFactory
            .selectFrom(chatEntity)
            .where(
                chatEntity.postId.eq(postId)
                    .and(chatEntity.participant1.id.eq(participantId1))
                    .and(chatEntity.isAnonymous1.eq(isAnonymous1))
                    .and(chatEntity.participant2.id.eq(participantId2))
                    .and(chatEntity.isAnonymous2.eq(isAnonymous2))
            )
            .fetchOne()
    }

    override fun findByBothParticipantId(participantId1: Long, participantId2: Long) : ChatEntity? {
        return jpaQueryFactory
            .selectFrom(chatEntity)
            .where(
                chatEntity.isAnonymous1.isFalse
                    .and(chatEntity.isAnonymous2.isFalse)
                    .and(
                        (chatEntity.participant1.id.eq(participantId1).and(chatEntity.participant2.id.eq(participantId2)))
                            .or(chatEntity.participant1.id.eq(participantId2).and(chatEntity.participant2.id.eq(participantId1)))
                    )
            )
            .fetchOne()
    }

    override fun findAllByParticipantId(participantId: Long): List<ChatEntity> {
        return jpaQueryFactory
            .selectFrom(chatEntity)
            .where(
                chatEntity.participant1.id.eq(participantId)
                    .or(chatEntity.participant2.id.eq(participantId))
            )
            .fetch()
    }

}