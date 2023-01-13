package com.wafflytime.post.service

import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.type.BoardType
import com.wafflytime.common.S3Service
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.exception.WafflyTime409
import com.wafflytime.post.database.*
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.dto.*
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostService(
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val postLikeRepository: PostLikeRepository,
    private val scrapRepository: ScrapRepository,
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

        val post: PostEntity = postRepository.save(
            PostEntity(
            title = request.title,
            contents = request.contents,
            images = getImagesEntityFromS3ImageUrl(s3ImageUrlDtoList),
            writer = user,
            board = board,
            isQuestion = request.isQuestion,
            isWriterAnonymous = request.isWriterAnonymous
        )
        )
        return PostResponse.of(post, s3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun getPost(boardId: Long, postId: Long): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        return PostResponse.of(post, s3Service.getPreSignedUrlsFromS3Keys(post.images))
    }

    fun getPosts(boardId: Long, page: Int, size:Int): List<PostResponse> {
        val sort = Sort.by(Sort.Direction.DESC, "createdAt")
        return postRepository.findAllByBoardId(boardId, PageRequest.of(page, size, sort)).content.map {
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

        val updatedS3ImageUrlDtoList = s3Service.updateImageRequest(post.images, request)
        post.update(request, getImagesEntityFromS3ImageUrl(updatedS3ImageUrlDtoList))
        return PostResponse.of(post, updatedS3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun validateBoardAndPost(boardId: Long, postId: Long) : PostEntity {
        val post: PostEntity = postRepository.findByIdOrNull(postId) ?: throw WafflyTime404("post id가 존재하지 않습니다")
        if (post.board.id != boardId) throw  WafflyTime400("board id와 post id가 매치되지 않습니다 : 해당 게시판에 속한 게시물이 아닙니다")
        return post
    }

    fun getImagesEntityFromS3ImageUrl(s3ImageUrlDtoList: MutableList<S3ImageUrlDto>?) : Map<String, ImageColumn>? {
        return s3ImageUrlDtoList?.map { it.fileName to ImageColumn.of(it) }?.toMap()
    }

    @Transactional
    fun likePost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("user id가 존재하지 않습니다")

        // 에타는 좋아요 취소가 안됨
        postLikeRepository.findByPostIdAndUserId(postId, userId)?.let {
            throw WafflyTime409("이미 공감한 댓글입니다")
        }

        postLikeRepository.save(PostLikeEntity(user = user, post = post))
        post.nLikes++
        return PostResponse.of(post)
    }

    @Transactional
    fun scrapPost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("user id가 존재하지 않습니다")

        scrapRepository.findByPostIdAndUserId(postId, userId)?.let {
            throw WafflyTime409("이미 스크랩한 게시물입니다")
        }
        scrapRepository.save(ScrapEntity(user = user, post = post))
        post.nScraps++
        return PostResponse.of(post)
    }
}