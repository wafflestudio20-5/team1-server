package com.wafflytime.user.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthToken(
    @JsonProperty("access_token")
    val accessToken: String,
)