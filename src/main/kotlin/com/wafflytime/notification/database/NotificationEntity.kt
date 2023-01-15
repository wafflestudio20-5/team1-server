package com.wafflytime.notification.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name="notification")
class NotificationEntity(
    var relatedUrl: String? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val receiver: UserEntity,

    var content: String,

    @field:NotNull @Enumerated(EnumType.STRING)
    val notificationType: NotificationType,
    var isRead: Boolean,

    ) : BaseTimeEntity()