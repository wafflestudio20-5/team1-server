package com.wafflytime.notification.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.notification.dto.NotificationInfo
import com.wafflytime.notification.type.NotificationType
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name="notification")
class NotificationEntity(


    /**
     * Reply, Board, 쪽지 등 notification이 필요한 도메인들은 각각 redirect 에 필요한 정보들이 다르다.
     * 각 domain 별로 어떤 형태의 json이든 db column으로 저장하기 위해 NorificationInfo 같은 interface를 사용하였다.
     * 하나의 class를 만들고 domain이 추가될 때마다 필드가 추가되는게 귀찮고, reply에서 필요한 정보들이 쪽지에서는 다 null로 저장시키기 때문에
        이를 피하고자 interface를 사용하였는데, 오히려 불편한 면도 있다.
     * JpaNotificationRedirectInfoConverter 구현을 살펴보면, interface는 deserialize가 안되기 때문에 NotificationType에
        따라서 다르게 TypeReference하고 있다
     * interface를 사용하는게 오히려 비효율적이라 판단되면, entity column에 replyInfo, boardInfo, 쪽지Info 각각 가지도록 만들어도 될 것 같다
     */
    @Column(length = 100, columnDefinition = "json")
    @Convert(converter = JpaNotificationInfoConverter::class)
    var info: NotificationInfo? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="wafflytime_user_id")
    val receiver: UserEntity,

    var content: String,

    @field:NotNull @Enumerated(EnumType.STRING)
    val notificationType: NotificationType,
    var isRead: Boolean,

    ) : BaseTimeEntity() {

    fun updateIsRead() {
        isRead = true
    }
}