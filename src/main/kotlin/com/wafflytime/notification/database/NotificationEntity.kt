package com.wafflytime.notification.database

import com.vladmihalcea.hibernate.type.json.JsonType
import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.notification.dto.NotificationRedirectInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type

@Entity
@Table(name="notification")
class NotificationEntity(

    @Column(length = 100, columnDefinition = "json")
    @Type(JsonType::class)
    var notificationRedirectInfo: NotificationRedirectInfo? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val receiver: UserEntity,

    var content: String,

    @field:NotNull @Enumerated(EnumType.STRING)
    val notificationType: NotificationType,
    var isRead: Boolean,

    ) : BaseTimeEntity()