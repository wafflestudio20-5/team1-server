package com.wafflytime.board.dto

import com.wafflytime.notification.dto.NotificationRedirectInfo
import com.wafflytime.notification.type.NotificationType


// 즐겨찾기를 한 게시판에 대해 Notification 까지 할 시간은 없을 수도 있을 것 같습니다.
// 이 클래스를 만들어둔 이유는 다른 종류의 알림을 만들 때, interface 를 활용해 아래와 같은 class 들을 반드시 만들어야 함을 알려주기 위한 파일입니다
// 당장은 이 파일 속 클래스들은 사용 안합니다

data class BoardNotificationRedirectInfo(
    val boardId: Long,
    val boardTitle: String,
    val postId: Long,
    override val notificationType: NotificationType = NotificationType.BOARD
) : NotificationRedirectInfo {
    constructor() : this(-1, "", -1)
}