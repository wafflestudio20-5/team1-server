package com.wafflytime.post.database

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository


interface PostRepository : JpaRepository<PostEntity, Long> {
    fun findAllByBoardId(boardId: Long, pageable: Pageable) : Page<PostEntity>
    fun findAllByWriterId(writerId: Long, pageable: Pageable): Page<PostEntity>

}