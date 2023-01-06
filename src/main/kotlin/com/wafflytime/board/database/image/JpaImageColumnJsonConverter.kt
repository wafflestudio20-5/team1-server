package com.wafflytime.board.database.image

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter


class JpaImageColumnJsonConverter : AttributeConverter<MutableList<ImageColumn>, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: MutableList<ImageColumn>?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): MutableList<ImageColumn>? {
        if (dbData == null) return null
        return mapper.readValue(dbData, object : TypeReference<MutableList<ImageColumn>>(){}).toMutableList()
    }
}