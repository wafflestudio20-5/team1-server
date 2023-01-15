package com.wafflytime.notification.database

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflytime.board.dto.BoardNotificationRedirectInfo
import com.wafflytime.notification.dto.NotificationRedirectInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.reply.dto.ReplyNotificationRedirectInfo
import jakarta.persistence.AttributeConverter
import org.json.JSONObject


class JpaNotificationRedirectInfoConverter : AttributeConverter<NotificationRedirectInfo, String> {
    private val mapper: ObjectMapper = ObjectMapper()

    // Map에 class 저장해두고 TypeReference<T>를 if-else가 아닌 동적으로 설정하고 싶지만 실패
    private val notificationRedirectInfoMap = mapOf(
        NotificationType.BOARD to BoardNotificationRedirectInfo::class,
        NotificationType.REPLY to ReplyNotificationRedirectInfo::class
    )

    override fun convertToDatabaseColumn(attribute: NotificationRedirectInfo?): String? {
        if (attribute == null) return null
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): NotificationRedirectInfo? {
        if (dbData == null) return null
        val notificationType = NotificationType.valueOf(JSONObject(dbData).getString("notificationType"))

        if (notificationType == NotificationType.REPLY) {
            return mapper.readValue(dbData, object : TypeReference<ReplyNotificationRedirectInfo>(){})
        } else if (notificationType == NotificationType.BOARD) {
            return mapper.readValue(dbData, object : TypeReference<BoardNotificationRedirectInfo>(){})
        } else {
            throw NotImplementedError("$notificationType 에 대해서는 구현되지 않았습니다")
        }
    }
}