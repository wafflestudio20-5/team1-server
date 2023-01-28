package com.wafflytime.chat.database

import com.wafflytime.common.BaseTimeEntity
import com.wafflytime.user.info.database.UserEntity
import jakarta.persistence.*

@Entity
@Table(name = "chat")
class ChatEntity(
    val postId: Long?,
    @ManyToOne
    @JoinColumn(name = "participant1_id")
    val participant1: UserEntity,
    val isAnonymous1: Boolean,
    var unread1: Int = 0,
    var blocked1: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "participant2_id")
    val participant2: UserEntity,
    val isAnonymous2: Boolean,
    var unread2: Int = 0,
    var blocked2: Boolean = false,
    @OneToMany(mappedBy = "chat", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    val messages: MutableList<MessageEntity> = mutableListOf()
): BaseTimeEntity() {

    fun isBlocked() = blocked1 || blocked2

    fun addMessage(message: MessageEntity) {
        messages.add(message)

        when (message.sender) {
            participant1 -> unread2++
            participant2 -> unread1++
        }
    }

    fun getSenderAndReceiver(senderId: Long): Pair<UserEntity, UserEntity> {
        return when (senderId) {
            participant1.id -> Pair(participant1, participant2)
            participant2.id -> Pair(participant2, participant1)
            else -> throw TODO()
        }
    }

}