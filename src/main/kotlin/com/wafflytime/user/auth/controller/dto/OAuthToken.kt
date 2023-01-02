package com.wafflytime.user.auth.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthToken(
    @JsonProperty("access_token")
    val accessToken: String,
)