package com.wafflytime.database

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
class TempUserEntity(
    var nickname: String,
    var snuMail: String? = null,
) : BaseTimeEntity() {
}

