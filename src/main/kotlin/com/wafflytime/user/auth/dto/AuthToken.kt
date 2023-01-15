package com.wafflytime.user.auth.dto

data class AuthToken(val accessToken: String, val refreshToken: String)