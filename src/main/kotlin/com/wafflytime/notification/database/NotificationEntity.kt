package com.wafflytime.notification.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.notification.dto.NotificationInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name="notification")
class NotificationEntity(

    @Column(length = 100, columnDefinition = "json")
    @Convert(converter = JpaNotificationInfoConverter::class)
    var info: NotificationInfo? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val receiver: UserEntity,

    var content: String,
    @Column(columnDefinition = "datetime(6) default '1999-01-01'")
    var contentCreatedAt: LocalDateTime? = null,

    @field:NotNull @Enumerated(EnumType.STRING)
    val notificationType: NotificationType,
    var isRead: Boolean,

    ) : BaseTimeEntity() {

    fun updateIsRead() {
        isRead = true
    }
}