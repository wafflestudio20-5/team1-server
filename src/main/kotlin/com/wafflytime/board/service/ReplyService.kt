package com.wafflytime.board.service

import com.wafflytime.board.database.*
import com.wafflytime.board.dto.CreateReplyRequest
import com.wafflytime.board.dto.ReplyResponse
import com.wafflytime.board.dto.ReplyWriterResponse
import com.wafflytime.board.dto.UpdateReplyRequest
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
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
    private val replyWriterRepository: ReplyWriterRepository,
    private val replyWriterRepositorySupport: ReplyWriterRepositorySupport,
) {
    @Transactional
    fun createReply(userId: Long, boardId: Long, postId: Long, request: CreateReplyRequest): ReplyResponse {
        val post = postService.validateBoardAndPost(boardId, postId)
        val user = userRepository.findByIdOrNull(userId)!!
        val parent = request.parent?.let {
            replyRepository.findByIdOrNull(request.parent) ?: throw WafflyTime404("해당하는 부모 댓글이 없습니다")
        }
        if (parent != null && parent.post != post) throw WafflyTime400("부모 댓글이 다른 글에 있습니다")

        val anonymousId = replyWriterRepositorySupport.getAnonymousId(post, user)
            ?: let {
                replyWriterRepository.save(
                    ReplyWriterEntity(
                        post = post,
                        writer = user,
                        anonymousId = replyWriterRepositorySupport.countReplyIds(post) + 1,
                    )
                ).anonymousId
            }

        val reply = replyRepository.save(
            ReplyEntity(
                contents = request.contents,
                writer = user,
                post = post,
                replyGroup = parent?.replyGroup ?: (commentCount(post) + 1),
                replyOrder = commentCount(parent) + 1,
                mention = parent,
                isRoot = (parent == null),
                isWriterAnonymous = request.isWriterAnonymous,
                anonymousId = anonymousId,
            )
        )

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
        val reply = validateBoardAndPostAndReply(boardId, postId, replyId)
        if (userId != reply.writer.id) throw WafflyTime401("댓글 작성자가 아닌 유저는 댓글을 수정할 수 없습니다")
        reply.update(request.contents, request.isWriterAnonymous)
        return replyToResponse(reply)
    }

    @Transactional
    fun deleteReply(userId: Long, boardId: Long, postId: Long, replyId: Long) {
        val reply = validateBoardAndPostAndReply(boardId, postId, replyId)
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("user id가 존재하지 않습니다")

        if (userId == reply.writer.id || user.isAdmin) {
            reply.delete()
        } else {
            throw WafflyTime401("댓글을 삭제할 권한이 없습니다")
        }
    }

    fun getReply(boardId: Long, postId: Long, replyId: Long): ReplyResponse {
        val reply = validateBoardAndPostAndReply(boardId, postId, replyId)
        return replyToResponse(reply)
    }

    fun getReplies(boardId: Long, postId: Long, page: Long, size: Long): List<ReplyResponse> {
        val post = postService.validateBoardAndPost(boardId, postId)
        return replyRepositorySupport.getReplies(post, page, size).map {
            replyToResponse(it)
        }
    }

    private fun commentCount(post: PostEntity): Long {
        return replyRepositorySupport.countReplies(post)
    }

    private fun commentCount(reply: ReplyEntity?): Long {
        return reply?.let {
            replyRepositorySupport.countChildReplies(reply.post, reply.replyGroup)
        } ?: 0
    }

    private fun replyToReplyWriter(reply: ReplyEntity?): ReplyWriterResponse? {
        return reply?.let {
            ReplyWriterResponse(
                writerId = reply.writer.id,
                anonymousId = reply.anonymousId,
                isWriterAnonymous = reply.isWriterAnonymous,
            )
        }
    }

    private fun validateBoardAndPostAndReply(boardId: Long, postId: Long, replyId: Long): ReplyEntity {
        postService.validateBoardAndPost(boardId, postId)
        val reply = replyRepository.findByIdOrNull(replyId) ?: throw WafflyTime404("reply id가 존재하지 않습니다")
        if (reply.post.id != postId) throw WafflyTime400("post id와 reply id가 매치되지 않습니다 : 해당 게시글에 속한 댓글이 아닙니다")
        return reply
    }

    private fun replyToResponse(reply: ReplyEntity): ReplyResponse {
        return ReplyResponse(
            replyId = reply.id,
            writer = replyToReplyWriter(reply)!!,
            parent = if (reply.isRoot) null else replyToReplyWriter(
                replyRepositorySupport.findParent(
                    reply.post,
                    reply.replyGroup
                )
            ),
            mention = replyToReplyWriter(reply.mention),
            contents = reply.contents,
            isDeleted = reply.isDeleted
        )
    }
}