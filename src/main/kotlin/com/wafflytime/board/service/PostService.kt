package com.wafflytime.board.service

import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.database.PostEntity
import com.wafflytime.board.database.PostRepository
import com.wafflytime.board.dto.CreatePostRequest
import com.wafflytime.board.dto.PostResponse
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostService(
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createPost(userId: Long, boardId: Long, request: CreatePostRequest): PostResponse {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw WafflyTime404("board id가 존재하지 않습니다")
        val user: UserEntity = userRepository.findByIdOrNull(userId)!!

        if (!board.allowAnonymous && request.isAnonymous) throw WafflyTime404("이 게시판은 익명으로 게시글을 작성할 수 없습니다")

        val post: PostEntity = postRepository.save(PostEntity(
            title = request.title,
            contents = request.contents,
            writer = user,
            board = board,
            isQuestion = request.isQuestion,
            isWriterAnonymous = request.isAnonymous)
        )
        return PostResponse.of(post)
    }

    fun getPost(boardId: Long, postId: Long): PostResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw WafflyTime404("post id에 해당한는 post를 찾을 수 없습니다")
        if (post.board.id != boardId) throw  WafflyTime400("board id와 post id가 매치되지 않습니다 : 해당 게시판에 속한 게시물이 아닙니다")
        return PostResponse.of(post)
    }

    fun getPosts(boardId: Long): List<PostResponse> {
        TODO("Not yet implemented")
    }
}