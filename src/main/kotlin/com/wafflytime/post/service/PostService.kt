package com.wafflytime.post.service

import com.wafflytime.board.dto.HomePostResponse
import com.wafflytime.board.service.BoardService
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType
import com.wafflytime.common.CursorPage
import com.wafflytime.common.DoubleCursorPage
import com.wafflytime.common.RedisService
import com.wafflytime.common.S3Service
import com.wafflytime.exception.DoubleCursorMismatch
import com.wafflytime.post.database.*
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.dto.*
import com.wafflytime.post.exception.*
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val scrapRepository: ScrapRepository,
    private val boardService: BoardService,
    private val userService: UserService,
    private val s3Service: S3Service,
    private val redisService: RedisService,
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
        if (board.category in listOf(BoardCategory.BASIC, BoardCategory.CAREER)) {
            redisService.save(post)
        }
        return PostResponse.of(userId, post, s3ImageUrlDtoList?.map { ImageResponse.of(it) })
    }

    fun getPost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val post = validateBoardAndPost(boardId, postId)
        return PostResponse.of(userId, post, s3Service.getPreSignedUrlsFromS3Keys(post.images))
    }

    fun getPosts(userId: Long, boardId: Long, page: Long, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByBoardId(boardId, page, size).map {
            PostResponse.of(
                userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images)
            )
        }
    }

    fun getPosts(userId: Long, boardId: Long, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByBoardId(boardId, cursor, size).map {
            PostResponse.of(
                userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images)
            )
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
            redisService.updateCacheByDeletedPost(post)
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
        redisService.updateCacheByUpdatedPost(post)
        return PostResponse.of(userId, post, updatedS3ImageUrlDtoList?.map { ImageResponse.of(it) })
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
        redisService.updateCacheByLikeOrReplyPost(post)
        return PostResponse.of(userId, post)
    }

    @Transactional
    fun scrapPost(userId: Long, boardId: Long, postId: Long): PostResponse {
        val (post, user) = validateLikeScrapPost(userId, boardId, postId)
        scrapRepository.findByPostIdAndUserId(postId, userId)?.let {
            throw AlreadyScrapped
        }
        scrapRepository.save(ScrapEntity(user = user, post = post))
        post.nScraps++
        return PostResponse.of(userId, post)
    }

    fun validateLikeScrapPost(userId: Long, boardId: Long, postId: Long): Pair<PostEntity, UserEntity> {
        val post = validateBoardAndPost(boardId, postId)
        val user = userService.getUser(userId)
        if (post.writer.id == userId) throw ForbiddenLikeScrap
        return Pair(post, user)
    }

    fun getHotPosts(userId: Long, page: Long, size: Long): CursorPage<PostResponse> {
        return postRepository.getHotPosts(page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun getHotPosts(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return postRepository.getHotPosts(cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun getBestPosts(userId: Long, page: Long, size: Long): DoubleCursorPage<PostResponse> {
        return postRepository.getBestPosts(page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun getBestPosts(userId: Long, first: Long?, second: Long?, size: Long): DoubleCursorPage<PostResponse> {
        if ((first == null) != (second == null)) throw DoubleCursorMismatch
        val cursor = first?.let { Pair(it, second!!) }

        return postRepository.getBestPosts(cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun searchPosts(userId: Long, keyword: String, page: Long, size: Long): CursorPage<PostResponse> {
        return postRepository.findPostsByKeyword(keyword, page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun searchPosts(userId: Long, keyword: String, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return postRepository.findPostsByKeyword(keyword, cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun searchPostsInBoard(userId: Long, boardId: Long, keyword: String, page: Long, size: Long): CursorPage<PostResponse> {
        boardService.getBoardEntity(boardId)
        return postRepository.findPostsInBoardByKeyword(boardId, keyword, page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun searchPostsInBoard(userId: Long, boardId: Long, keyword: String, cursor: Long?, size: Long): CursorPage<PostResponse> {
        boardService.getBoardEntity(boardId)
        return postRepository.findPostsInBoardByKeyword(boardId, keyword, cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    fun getHomePostsTest(): List<HomePostResponse> {
        return redisService.getLatestPostsGroup()
    }

    fun getLatestPostsByCategory(userId: Long, category: BoardCategory, size: Int): List<PostResponse> {
        return postRepository.findLatestPostsByCategory(category, size).map { PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images)) }
    }
}