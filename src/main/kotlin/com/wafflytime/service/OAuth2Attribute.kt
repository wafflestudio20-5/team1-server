package com.wafflytime.service

data class OAuth2Attribute(
    private val attributes: Map<String, Any>,
    private val attributeKey: String,
    private val email: String,
) {
    companion object {
        fun of(provider: String, attributeKey: String, attributes: Map<String, Any>): OAuth2Attribute {
            when (provider) {
                "google" -> return ofGoogle(attributeKey, attributes)
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
    }

    fun toMap() = mutableMapOf<String, Any>(
        "id" to attributeKey,
        "key" to attributeKey,
        "email" to email,
    )
}