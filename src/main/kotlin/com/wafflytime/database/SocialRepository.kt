package com.wafflytime.database

import org.springframework.data.jpa.repository.JpaRepository

interface SocialRepository : JpaRepository<SocialEntity, Long> {
    fun findByProviderAndEmail(provider: String, email: String): SocialEntity?
}