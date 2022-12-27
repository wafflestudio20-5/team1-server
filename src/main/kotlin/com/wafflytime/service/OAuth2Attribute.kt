package com.wafflytime.service

data class OAuth2Attribute(
    private val attributes: Map<String, Any>,
    private val attributeKey: String,
    private val email: String,
) {
    companion object {
        fun of(provider: String, attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return when (provider) {
                "google" -> ofGoogle(attributeKey, attributes)
                "facebook" -> ofFacebook(attributeKey, attributes)
                else -> throw Exception()
            }
        }

        private fun ofGoogle(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            return OAuth2Attribute(
                attributes = attributes,
                attributeKey = attributeKey,
                email = attributes["email"] as String
            )
        }

        private fun ofFacebook(attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
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