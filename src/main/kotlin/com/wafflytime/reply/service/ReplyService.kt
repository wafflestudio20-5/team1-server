package com.wafflytime.reply.service

import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.service.PostService
import com.wafflytime.reply.database.ReplyEntity
import com.wafflytime.reply.database.ReplyRepository
import com.wafflytime.reply.database.ReplyRepositorySupport
import com.wafflytime.reply.dto.CreateReplyRequest
import com.wafflytime.reply.dto.ReplyResponse
import com.wafflytime.reply.dto.UpdateReplyRequest
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ReplyService(
    private val userRepository: UserRepository,
    private val postService: PostService,
    private val replyRepository: ReplyRepository,
    private val replyRepositorySupport: ReplyRepositorySupport,
) {
    @Transactional
    fun createReply(userId: Long, boardId: Long, postId: Long, request: CreateReplyRequest): ReplyResponse {
        val post = postService.validateBoardAndPost(boardId, postId)
        val user = userRepository.findByIdOrNull(userId)!!
        val mention = request.mention?.let {
            replyRepository.findByIdOrNull(request.mention) ?: throw WafflyTime404("해당하는 부모 댓글이 없습니다")
        }
        if (mention != null && mention.post != post) throw WafflyTime400("부모 댓글이 다른 글에 있습니다")
        if (mention?.isDeleted == true) throw WafflyTime404("삭제된 댓글에 답글을 달 수 없습니다")

        val reply = replyRepository.save(
            ReplyEntity(
                contents = request.contents,
                writer = user,
                post = post,
                replyGroup = mention?.replyGroup ?: (commentCount(post) + 1),
                mention = mention,
                isRoot = (mention == null),
                isWriterAnonymous = request.isWriterAnonymous,
                anonymousId = replyRepositorySupport.getAnonymousId(post, user),
                isPostWriter = (post.id == user.id)
            )
        )

        post.replies++

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
        if (userId != reply.writer.id) throw WafflyTime401("댓글 작성자가 아닌 유저는 댓글을 수정할 수 없습니다")
        if (reply.isDeleted) throw WafflyTime404("삭제된 댓글을 수정할 수 없습니다")
        reply.update(request.contents, request.isWriterAnonymous)
        return replyToResponse(reply)
    }

    @Transactional
    fun deleteReply(userId: Long, boardId: Long, postId: Long, replyId: Long) {
        val post = postService.validateBoardAndPost(boardId, postId)
        val reply = validatePostAndReply(postId, replyId)
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("user id가 존재하지 않습니다")

        if (userId == reply.writer.id || user.isAdmin) {
            reply.delete()
            post.replies--

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
            throw WafflyTime401("댓글을 삭제할 권한이 없습니다")
        }
    }

    fun getReply(boardId: Long, postId: Long, replyId: Long): ReplyResponse {
        postService.validateBoardAndPost(boardId, postId)
        val reply = validatePostAndReply(postId, replyId)
        return replyToResponse(reply)
    }

    fun getReplies(boardId: Long, postId: Long, page: Long, size: Long): List<ReplyResponse> {
        val post = postService.validateBoardAndPost(boardId, postId)
        return replyRepositorySupport.getReplies(post, page, size).map {
            replyToResponse(it)
        }
    }

    private fun commentCount(post: PostEntity): Long {
        return replyRepositorySupport.getLastReplyGroup(post)
    }

    private fun validatePostAndReply(postId: Long, replyId: Long): ReplyEntity {
        val reply = replyRepository.findByIdOrNull(replyId) ?: throw WafflyTime404("reply id가 존재하지 않습니다")
        if (reply.post.id != postId) throw WafflyTime400("post id와 reply id가 매치되지 않습니다 : 해당 게시글에 속한 댓글이 아닙니다")
        return reply
    }

    private fun replyToResponse(reply: ReplyEntity): ReplyResponse {
        val mention = reply.mention
        return ReplyResponse(
            replyId = reply.id,
            writerId = reply.writer.id,
            nickname = if (reply.isWriterAnonymous) "익명${reply.anonymousId}" else reply.writer.nickname!!,
            mention = mention ?.let {
                if (mention.isWriterAnonymous) "익명${mention.anonymousId}" else mention.writer.nickname!!
            },
            contents = reply.contents,
            isDeleted = reply.isDeleted,
        )
    }
}