package com.wafflytime.board.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflytime.board.database.QBoardEntity.boardEntity
import com.wafflytime.board.type.BoardCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component


interface BoardRepository : JpaRepository<BoardEntity, Long>, BoardRepositorySupport {
    fun findByTitle(title: String) : BoardEntity?
}

interface BoardRepositorySupport {
    fun findHomeBoards(): List<BoardEntity>
    fun findBoardsByKeyword(keyword: String): List<BoardEntity>
}

@Component
class BoardRepositorySupportImpl(
    private val queryFactory: JPAQueryFactory
) : BoardRepositorySupport {
    override fun findHomeBoards(): List<BoardEntity> {
        return queryFactory
            .selectFrom(boardEntity)
            .where(boardEntity.category.`in`(BoardCategory.BASIC, BoardCategory.CAREER))
            .fetch()
    }

    // 에타에서 게시판 검색은 pageable 하게 처리하지 않음
    override fun findBoardsByKeyword(keyword: String) : List<BoardEntity> {
        return queryFactory
            .selectFrom(boardEntity)
            .where(boardEntity.title.contains(keyword))
            .fetch()
    }

}