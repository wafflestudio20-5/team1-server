package com.wafflytime.chat.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "message")
class MessageEntity(
    @ManyToOne
    @JoinColumn(name = "chat_id")
    val chat: ChatEntity,
    @ManyToOne
    @JoinColumn(name = "sender_id")
    val sender: UserEntity? = null, // null 인 경우 안내 메세지
    @field:NotBlank
    val content: String,
): BaseTimeEntity() {
}