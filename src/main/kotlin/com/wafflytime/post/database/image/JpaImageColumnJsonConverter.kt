package com.wafflytime.post.database.image

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter


class JpaImageColumnJsonConverter : AttributeConverter<Map<String, ImageColumn>, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, ImageColumn>?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, ImageColumn>? {
        if (dbData == null) return null
        return mapper.readValue(dbData, object : TypeReference<Map<String, ImageColumn>>(){})
    }
}