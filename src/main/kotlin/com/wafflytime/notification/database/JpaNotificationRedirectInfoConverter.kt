package com.wafflytime.notification.database

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.board.dto.BoardNotificationInfo
import com.wafflytime.notification.dto.NotificationInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.reply.dto.ReplyNotificationInfo
import jakarta.persistence.AttributeConverter
import org.json.JSONObject


class JpaNotificationRedirectInfoConverter : AttributeConverter<NotificationInfo, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: NotificationInfo?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): NotificationInfo? {
        if (dbData == null) return null
        val notificationType = NotificationType.valueOf(JSONObject(dbData).getString("notificationType"))

        if (notificationType == NotificationType.REPLY) {
            println("reply parse!")
            return mapper.readValue(dbData, object : TypeReference<ReplyNotificationInfo>(){})
        } else if (notificationType == NotificationType.BOARD) {
            println("board parse!")
            return mapper.readValue(dbData, object : TypeReference<BoardNotificationInfo>(){})
        } else {
            throw NotImplementedError("$notificationType 에 대해서는 구현되지 않았습니다")
        }
    }
}