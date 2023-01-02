package com.wafflytime.board.database

import org.springframework.data.jpa.repository.JpaRepository


interface LikeRepository : JpaRepository<LikeEntity, Long> {

}