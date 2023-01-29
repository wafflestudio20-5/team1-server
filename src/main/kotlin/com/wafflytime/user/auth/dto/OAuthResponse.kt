package com.wafflytime.user.auth.dto

data class OAuthResponse(
    val authToken: AuthToken?,
    val needNickname: Boolean,
)