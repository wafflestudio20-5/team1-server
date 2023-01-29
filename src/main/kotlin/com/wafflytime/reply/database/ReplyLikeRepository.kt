package com.wafflytime.reply.database

import org.springframework.data.jpa.repository.JpaRepository

interface ReplyLikeRepository : JpaRepository<ReplyLikeEntity, Long> {
    fun findByReplyIdAndUserId(replyId: Long, userId: Long) : ReplyLikeEntity?
}