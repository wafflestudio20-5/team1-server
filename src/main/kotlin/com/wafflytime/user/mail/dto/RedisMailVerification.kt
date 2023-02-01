package com.wafflytime.user.mail.dto

data class RedisMailVerification(
    var code: String,
    var email: String,
) {
    constructor(): this("", "")
}
