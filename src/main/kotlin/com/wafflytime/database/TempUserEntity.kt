package com.wafflytime.database

import jakarta.persistence.Entity

@Entity
class TempUserEntity(
    var nickname: String,
    var univEmail: String? = null,
) : BaseTimeEntity() {
}

