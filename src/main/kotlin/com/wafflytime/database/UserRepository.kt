package com.wafflytime.database

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<TempUserEntity, Long> {
    fun findByUnivEmail(univEmail: String) : TempUserEntity?
}