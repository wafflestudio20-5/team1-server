package com.wafflytime.database

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUnivEmail(univEmail: String) : UserEntity?
}