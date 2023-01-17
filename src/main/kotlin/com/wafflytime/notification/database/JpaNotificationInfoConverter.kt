package com.wafflytime.notification.database

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.notification.dto.NotificationInfo
import jakarta.persistence.AttributeConverter


class JpaNotificationInfoConverter : AttributeConverter<NotificationInfo, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: NotificationInfo?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): NotificationInfo? {
        if (dbData == null) return null
        return mapper.readValue(dbData, object : TypeReference<NotificationInfo>(){})
    }
}