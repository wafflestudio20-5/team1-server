package com.wafflytime.post.database

import org.springframework.data.jpa.repository.JpaRepository


interface ScrapRepository : JpaRepository<ScrapEntity, Long> {
}
