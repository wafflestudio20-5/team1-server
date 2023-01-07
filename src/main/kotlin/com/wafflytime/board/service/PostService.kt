package com.wafflytime.board.service

import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.database.PostEntity
import com.wafflytime.board.database.PostRepository
import com.wafflytime.board.dto.*
import com.wafflytime.board.type.BoardType
import com.wafflytime.board.database.image.ImageColumn
import com.wafflytime.board.dto.ImageResponse
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.common.S3Service
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostService(
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val s3Service: S3Service
) {

    @Transactional
    fun createPost(userId: Long, boardId: Long, request: CreatePostRequest) : PostResponse {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw WafflyTime404("board id가 존재하지 않습니다")
        val user: UserEntity = userRepository.findByIdOrNull(userId)!!

        if (board.type == BoardType.DEFAULT && request.title == null ) throw WafflyTime400("default 게시판은 title이 반드시 존재해야 됩니다")
        if (board.type in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO) && request.title != null ) {
            throw WafflyTime400("CUSTOM 게시판은 title이 존재하지 않습니다")
        }

        if (!board.allowAnonymous && request.isWriterAnonymous) throw WafflyTime404("이 게시판은 익명으로 게시글을 작성할 수 없습니다")

        val s3ImageUrlDtoList = s3Service.getPreSignedUrlsAndS3Urls(request.images)

        val post: PostEntity = postRepository.save(PostEntity(
            title = request.title,
            contents = request.contents,
            images = s3ImageUrlDtoList?.map { ImageColumn.of(it) }?.toMutableList(),
            writer = user,
            board = board,
            isQuestion = request.isQuestion,
            isWriterAnonymous = request.isWriterAnonymous
        ))
        return PostResponse.of(post, s3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun getPost(boardId: Long, postId: Long): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        return PostResponse.of(post, s3Service.getPreSignedUrlsFromS3Keys(post.images))
    }

    fun getPosts(boardId: Long, page: Int, size:Int): Page<PostResponse> {
        val sort = Sort.by(Sort.Direction.DESC, "createdAt")
        return postRepository.findAll(PageRequest.of(page, size, sort)).map {
            PostResponse.of(
                it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    @Transactional
    fun deletePost(userId: Long, boardId: Long, postId: Long): DeletePostResponse {
        val post = validateBoardAndPost(boardId, postId)
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("user id가 존재하지 않습니다")

        // 게시물 작성자, 게시판 주인, admin 만 게시물을 삭제 할 수 있다
        if (userId == post.writer.id || user.isAdmin || userId == post.board.owner!!.id) {
            s3Service.deleteFiles(post.images)
            postRepository.delete(post)
            return DeletePostResponse(
                boardId = boardId,
                boardTitle = post.board.title,
                postId = postId,
                postTitle = post.title
            )
        } else {
            throw WafflyTime401("게시물을 삭제할 권한이 없습니다")
        }
    }

    @Transactional
    fun updatePost(userId: Long, boardId: Long, postId: Long, request: UpdatePostRequest): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        if (userId != post.writer.id) throw WafflyTime401("게시물 작성자가 아닌 유저는 게시물을 수정할 수 없습니다")

        // 이미지 수정 로직은 개선이 필요함
        /** TODO(재웅): 현재 방식은 게시물 수정할 때 그 전에 있던 사진들은 다 삭제 하고 다시 업로드 하는 방식
        - 현재 방식은 클라이언트가 보통의 update request 처럼 '변화된' 필드에 데이터를 담아서 보내주는 것이 아닌, images 필드는
        반드시 유저의 게시물 그 상태를 그대로 전달해주어야 한다(사진이 바뀌지 않았더라도 사진 정보를 그대로 UpadateRequesst에 담아서).
        프론트에서 이걸 어떻게 전달해주는게 좋을지 프론트랑 얘개히보고 수정하면 좋을 듯 하다
        - 유저가 원래 있던 사진 유지하고 추가하는 작업이라면 과거에 push 했던 사진은 남겨두는 방식이 좋을 것 같은데 더 나은 방식으로 나중에 수정하자
         **/
        s3Service.deleteFiles(post.images)
        val s3ImageUrlDtoList = s3Service.getPreSignedUrlsAndS3Urls(request.images)
        post.update(request, s3ImageUrlDtoList?.map { ImageColumn.of(it) }?.toMutableList())
        return PostResponse.of(post, s3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun validateBoardAndPost(boardId: Long, postId: Long) : PostEntity {
        val post: PostEntity = postRepository.findByIdOrNull(postId) ?: throw WafflyTime404("post id가 존재하지 않습니다")
        if (post.board.id != boardId) throw  WafflyTime400("board id와 post id가 매치되지 않습니다 : 해당 게시판에 속한 게시물이 아닙니다")
        return post
    }
}