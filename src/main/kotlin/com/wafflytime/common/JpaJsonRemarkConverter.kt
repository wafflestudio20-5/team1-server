package com.wafflytime.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter

class JpaJsonRemarkConverter : AttributeConverter<MutableList<String>, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun convertToEntityAttribute(dbData: String?): MutableList<String>? {
        if (dbData == null) return null
        return mapper.readValue(dbData, object : TypeReference<MutableList<String>>(){})
    }
    override fun convertToDatabaseColumn(attribute: MutableList<String>?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }
}