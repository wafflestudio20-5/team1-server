package com.example.demo.database

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
class TempUserEntity(
    var nickname: String,
    var emailVerified: Boolean = false,
) : BaseTimeEntity() {
}

