package com.wafflytime.user.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "oauth2")
class OAuthProperties {
    val provider = mutableMapOf<String, Provider>()

    data class Provider(
        val clientId: String,
        val clientSecret: String = "",
        val tokenUri: String,
        val userInfoUri: String,
        val redirectUri: String,
    )
}

@Configuration
@EnableConfigurationProperties(OAuthProperties::class)
data class OAuthConfig(
    val properties: OAuthProperties,
)