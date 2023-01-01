package com.wafflytime.user.auth.service

import com.wafflytime.exception.WafflyTime400

data class OAuth2Attribute(
    private val attributes: Map<String, Any>,
    private val attributeKey: String,
    private val email: String,
) {
    companion object {
        fun of(provider: String, attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return when (provider) {
                "google" -> ofGoogle(attributeKey, attributes)
                "naver" -> ofNaver("id", attributes)
                "kakao" -> ofKakao("email", attributes)
                "github" -> ofGithub(attributeKey, attributes)
                else -> throw WafflyTime400("제공하지 않는 소셜 로그인 서비스입니다.")
            }
        }

        private fun ofGoogle(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return OAuth2Attribute(
                attributes = attributes,
                attributeKey = attributeKey,
                email = attributes["email"] as String
            )
        }

        private fun ofNaver(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            val response= attributes["response"] as Map<String, Any>
            return OAuth2Attribute(
                attributes = response,
                attributeKey = attributeKey,
                email = response["email"] as String
            )
        }

        private fun ofKakao(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            val kakaoAccount= attributes["kakao_account"] as Map<String, Any>
            return OAuth2Attribute(
                attributes = kakaoAccount,
                attributeKey = attributeKey,
                email = kakaoAccount["email"] as String
            )
        }

        private fun ofGithub(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return OAuth2Attribute(
                attributes = attributes,
                attributeKey = attributeKey,
                email = attributes["email"] as String
            )
        }
    }

    fun toMap() = mutableMapOf<String, Any>(
        "id" to attributeKey,
        "key" to attributeKey,
        "email" to email,
    )
}