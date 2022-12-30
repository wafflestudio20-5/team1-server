package com.wafflytime.user.info.database

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByLoginId(loginId: String) : UserEntity?
    fun findByUnivEmail(univEmail: String) : UserEntity?
    fun findBySocialEmail(socialEmail: String) : UserEntity?
}