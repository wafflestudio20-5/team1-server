package com.wafflytime.post.database

import org.springframework.data.jpa.repository.JpaRepository

interface PostLikeRepository : JpaRepository<PostLikeEntity, Long> {
    fun findByPostIdAndUserId(postId: Long, userId: Long) : PostLikeEntity?
}

