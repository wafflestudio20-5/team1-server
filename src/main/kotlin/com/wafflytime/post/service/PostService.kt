package com.wafflytime.post.service

import com.wafflytime.board.service.BoardService
import com.wafflytime.board.type.BoardType
import com.wafflytime.common.S3Service
import com.wafflytime.post.database.*
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.database.PostRepository
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.dto.*
import com.wafflytime.post.exception.*
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val scrapRepository: ScrapRepository,
    private val boardService: BoardService,
    private val userService: UserService,
    private val s3Service: S3Service
) {

    @Transactional
    fun createPost(userId: Long, boardId: Long, request: CreatePostRequest) : PostResponse {
        val board = boardService.getBoardEntity(boardId)
        val user = userService.getUser(userId)

        if (board.type == BoardType.DEFAULT && request.title == null) throw TitleRequired
        if (board.type in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO) && request.title != null) {
            throw TitleNotRequired
        }

        if (!board.allowAnonymous && request.isWriterAnonymous) throw AnonymousNotAllowed

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

    fun getPosts(boardId: Long, page: Int, size:Int): Page<PostResponse> {
        val sort = Sort.by(Sort.Direction.DESC, "createdAt")
        return postRepository.findAllByBoardId(boardId, PageRequest.of(page, size, sort)).map {
            PostResponse.of(
                it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    @Transactional
    fun deletePost(userId: Long, boardId: Long, postId: Long): DeletePostResponse {
        val post = validateBoardAndPost(boardId, postId)
        val user = userService.getUser(userId)

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
            throw ForbiddenPostRemoval
        }
    }

    @Transactional
    fun updatePost(userId: Long, boardId: Long, postId: Long, request: UpdatePostRequest): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        if (userId != post.writer.id) throw ForbiddenPostUpdate

        val updatedS3ImageUrlDtoList = s3Service.updateImageRequest(post.images, request)
        post.update(request, getImagesEntityFromS3ImageUrl(updatedS3ImageUrlDtoList))
        return PostResponse.of(post, updatedS3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun validateBoardAndPost(boardId: Long, postId: Long) : PostEntity {
        val post: PostEntity = postRepository.findByIdOrNull(postId) ?: throw PostNotFound
        if (post.board.id != boardId) throw BoardPostMismatch
        return post
    }

    fun getImagesEntityFromS3ImageUrl(s3PostImageUrlDtoList: MutableList<S3PostImageUrlDto>?) : Map<String, ImageColumn>? {
        return s3PostImageUrlDtoList?.map { it.fileName to ImageColumn.of(it) }?.toMap()
    }

    @Transactional
    fun likePost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val (post, user) = validateLikeScrapPost(userId, boardId, postId)

        // 에타는 좋아요 취소가 안됨
        postLikeRepository.findByPostIdAndUserId(postId, userId)?.let {
            throw AlreadyLiked
        }

        postLikeRepository.save(PostLikeEntity(user = user, post = post))
        post.nLikes++
        return PostResponse.of(post)
    }

    @Transactional
    fun scrapPost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val (post, user) = validateLikeScrapPost(userId, boardId, postId)
        scrapRepository.findByPostIdAndUserId(postId, userId)?.let {
            throw AlreadyScrapped
        }
        scrapRepository.save(ScrapEntity(user = user, post = post))
        post.nScraps++
        return PostResponse.of(post)
    }

    fun validateLikeScrapPost(userId: Long, boardId: Long, postId: Long): Pair<PostEntity, UserEntity> {
        val post = validateBoardAndPost(boardId, postId)
        val user = userService.getUser(userId)
        if (post.writer.id == userId) throw ForbiddenLikeScrap
        return Pair(post, user)
    }

    fun getHostPosts(page:Int, size:Int): Page<PostResponse> {
        return postRepository.getHotPosts(PageRequest.of(page, size)).map {
            PostResponse.of(it)
        }
    }

    fun getBestPosts(page: Int, size: Int): Page<PostResponse> {
        return postRepository.getBestPosts(PageRequest.of(page, size)).map {
            PostResponse.of(it)
        }
    }
}