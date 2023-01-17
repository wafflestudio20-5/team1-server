package com.wafflytime.notification.type



enum class NotificationType(val prefix: String) {
    BOARD("어제 가장 HOT 했던 글이에요: "),
    REPLY("새로운 댓글이 달렸어요: "),
    MESSAGE("쪽지가 왔어요: ")
}