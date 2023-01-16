package com.wafflytime.chat.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*

@Entity
@Table(name = "chat")
class ChatEntity(
    @ManyToOne
    @JoinColumn(name = "participant1_id")
    val participant1: UserEntity,
    val isAnonymous1: Boolean,
    var unread1: Int = 0,
    @ManyToOne
    @JoinColumn(name = "participant2_id")
    val participant2: UserEntity,
    val isAnonymous2: Boolean,
    var unread2: Int = 0,
    @OneToMany(mappedBy = "chat", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val messages: MutableList<MessageEntity> = mutableListOf()
): BaseTimeEntity() {

    fun addMessage(message: MessageEntity) {
        messages.add(message)

        when (message.sender) {
            participant1 -> unread2++
            participant2 -> unread1++
        }
    }

}