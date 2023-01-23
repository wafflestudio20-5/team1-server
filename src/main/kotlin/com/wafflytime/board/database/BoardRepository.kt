package com.wafflytime.board.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface BoardRepository : JpaRepository<BoardEntity, Long> {
    fun findByTitle(title: String) : BoardEntity?

    @Query(value = "select b from BoardEntity b where b.category in ('BASIC', 'CAREER') order by b.category")
    fun findHomeBoards(): List<BoardEntity>
}