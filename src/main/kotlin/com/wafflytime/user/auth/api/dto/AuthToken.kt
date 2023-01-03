package com.wafflytime.user.auth.api.dto

data class AuthToken(val accessToken: String, val refreshToken: String)