package com.wafflytime.board.database

import org.springframework.data.jpa.repository.JpaRepository


interface ScrapRepository : JpaRepository<ScrapEntity, Long> {
}
