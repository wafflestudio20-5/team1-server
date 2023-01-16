package com.wafflytime.reply.service

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
    private val replyRepository: ReplyRepository,
    private val replyRepositorySupport: ReplyRepositorySupport,
) {
    @Transactional
    fun createReply(userId: Long, boardId: Long, postId: Long, request: CreateReplyRequest): ReplyResponse {
        val post = postService.validateBoardAndPost(boardId, postId)
        val user = userService.getUser(userId)
        val parent = request.parent?.let { validatePostAndReply(postId, it) }

        if ((post.writer.id == user.id) && (post.isWriterAnonymous != request.isWriterAnonymous)) {
            throw WriterAnonymousFixed
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
        if (reply.isPostWriter && request.isWriterAnonymous != null) {
            throw WriterAnonymousFixed
        }
        reply.update(request.contents, request.isWriterAnonymous)
        return replyToResponse(reply)
    }

    @Transactional
    fun deleteReply(userId: Long, boardId: Long, postId: Long, replyId: Long) {
        val post = postService.validateBoardAndPost(boardId, postId)
        val reply = validatePostAndReply(postId, replyId)
        val user = userService.getUser(userId)

        // TODO: board 관리자도 추가 필요?
        if (userId == reply.writer.id || user.isAdmin) {
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
            isRoot = reply.isRoot,
            contents = reply.contents,
            isDeleted = reply.isDeleted,
            isPostWriter = reply.isPostWriter,
        )
    }
}