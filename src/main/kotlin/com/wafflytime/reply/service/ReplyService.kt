package com.wafflytime.reply.service

import com.wafflytime.common.DateTimeResponse
import com.wafflytime.common.RedisService
import com.wafflytime.notification.dto.NotificationDto
import com.wafflytime.notification.service.NotificationService
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.service.PostService
import com.wafflytime.reply.database.ReplyEntity
import com.wafflytime.reply.database.ReplyRepository
import com.wafflytime.reply.database.ReplyRepositorySupport
import com.wafflytime.reply.dto.CreateReplyRequest
import com.wafflytime.reply.dto.ReplyResponse
import com.wafflytime.reply.dto.UpdateReplyRequest
import com.wafflytime.reply.exception.*
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ReplyService(
    private val userService: UserService,
    private val postService: PostService,
    private val notificationService: NotificationService,
    private val replyRepository: ReplyRepository,
    private val replyRepositorySupport: ReplyRepositorySupport,
    private val redisService: RedisService
) {
    @Transactional
    fun createReply(userId: Long, boardId: Long, postId: Long, request: CreateReplyRequest): ReplyResponse {
        val post = postService.validateBoardAndPost(boardId, postId)
        val user = userService.getUser(userId)
        val parent = request.parent?.let {
            validatePostAndReply(postId, it)
        }

        val reply = replyRepository.save(
            ReplyEntity(
                contents = request.contents,
                writer = user,
                post = post,
                replyGroup = parent?.replyGroup ?: (commentCount(post) + 1),
                parentId = parent?.id,
                isRoot = (parent == null),
                isWriterAnonymous = request.isWriterAnonymous,
                anonymousId = replyRepositorySupport.getAnonymousId(post, user),
                isPostWriter = (post.writer.id == user.id)
            )
        )

        post.nReplies++

        // 일반 댓글이 달리면 게시물 작성자에게 알림 & 대댓글이 달리면 parent 댓글 작성자에게 알림
        // 게시물 작성자가 작성한 댓글은 알림이 가지 않음
        if (!reply.isPostWriter) {
            notificationService.send(NotificationDto.fromReply(receiver = parent?.writer ?: post.writer, reply=reply))
        }
        redisService.updateCacheByLikeOrReplyPost(post)

        return replyToResponse(reply)
    }

    @Transactional
    fun updateReply(
        userId: Long,
        boardId: Long,
        postId: Long,
        replyId: Long,
        request: UpdateReplyRequest
    ): ReplyResponse {
        postService.validateBoardAndPost(boardId, postId)
        val reply = validatePostAndReply(postId, replyId)
        if (userId != reply.writer.id) throw ForbiddenReplyUpdate

        reply.update(request.contents)
        // reply 수정은 알림이 가지 않는다
        return replyToResponse(reply)
    }

    @Transactional
    fun deleteReply(userId: Long, boardId: Long, postId: Long, replyId: Long) {
        val post = postService.validateBoardAndPost(boardId, postId)
        val board = post.board
        val reply = validatePostAndReply(postId, replyId)
        val user = userService.getUser(userId)

        if (userId == reply.writer.id || userId == board.owner?.id || user.isAdmin) {
            reply.delete()
            post.nReplies--

            val countChild = replyRepositorySupport.countChildReplies(post, reply.replyGroup)
            if (reply.isRoot && countChild == 0L) {
                reply.update(isDisplayed = false)
                return
            }
            if (!reply.isRoot) {
                reply.update(isDisplayed = false)
                val parent = replyRepositorySupport.findParent(post, reply.replyGroup)!!
                if (countChild == 0L) parent.update(isDisplayed = false)
            }
        } else {
            throw ForbiddenReplyRemoval
        }
    }

    fun getReply(boardId: Long, postId: Long, replyId: Long): ReplyResponse {
        postService.validateBoardAndPost(boardId, postId)
        val reply = validatePostAndReply(postId, replyId)
        return replyToResponse(reply)
    }

    fun getReplies(boardId: Long, postId: Long, page: Int, size: Int): Page<ReplyResponse> {
        val post = postService.validateBoardAndPost(boardId, postId)
        val pageRequest = PageRequest.of(page, size)
        return replyRepositorySupport.getReplies(post, pageRequest).map {
            replyToResponse(it)
        }
    }

    fun getReplyEntity(postId: Long, replyId: Long): ReplyEntity {
        return validatePostAndReply(postId, replyId)
    }

    private fun commentCount(post: PostEntity): Long {
        return replyRepositorySupport.getLastReplyGroup(post)
    }

    private fun validatePostAndReply(postId: Long, replyId: Long): ReplyEntity {
        val reply = replyRepository.findByIdOrNull(replyId) ?: throw ReplyNotFound
        if (reply.post.id != postId) throw PostReplyMismatch
        if (reply.isDeleted) throw ReplyDeleted
        return reply
    }

    private fun replyToResponse(reply: ReplyEntity): ReplyResponse {
        return ReplyResponse(
            replyId = reply.id,
            writerId = reply.writer.id,
            nickname = if (reply.isWriterAnonymous) {
                if (reply.isPostWriter) "익명(작성자)"
                else "익명${reply.anonymousId}"
            } else reply.writer.nickname,
            createdAt = DateTimeResponse.of(reply.createdAt!!),
            isRoot = reply.isRoot,
            contents = reply.contents,
            isDeleted = reply.isDeleted,
            isPostWriter = reply.isPostWriter,
        )
    }
}