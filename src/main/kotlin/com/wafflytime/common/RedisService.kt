package com.wafflytime.common

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.dto.HomePostResponse
import com.wafflytime.board.type.BoardType
import com.wafflytime.post.database.PostEntity
import com.wafflytime.post.database.PostRepository
import com.wafflytime.post.dto.HomePostDto
import com.wafflytime.post.dto.RedisPostDto
import kotlinx.coroutines.*
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val redisPostTemplate: RedisTemplate<String, RedisPostDto>,
    private val postRepository: PostRepository,
    private val s3Service: S3Service
) {
    @EventListener(ApplicationStartedEvent::class)
    fun pushInitialRedisCache() {
        postRepository.findHomePostsByQuery().forEach { save(it) }
    }

    fun save(post: PostEntity) {
        val boardKey = getBoardKey(post.board)
        val operations = redisPostTemplate.opsForList()
        operations.leftPush(boardKey, RedisPostDto.of(post))

        val maxSize = getMaxPostSize(post.board.type)
        val size = operations.size(boardKey) ?: 0
        if (size > maxSize) {
            operations.rightPop(boardKey, size-maxSize)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLatestPostsGroup() : List<HomePostResponse> {
        val keys = redisPostTemplate.keys("board:*")
        val future = mutableListOf<Deferred<HomePostResponse>>()
        keys.forEach {
            val boardInfo = parseBoardKey(it)
            val boardId = boardInfo.first
            val boardType = boardInfo.second
            val boardTitle = boardInfo.third
            future.add(
                CoroutineScope(Dispatchers.Default).async {
                    HomePostResponse.of(boardId = boardId, boardTitle=boardTitle, boardType=boardType,
                        posts = redisPostTemplate.opsForList().operations.opsForList().range(it, 0, getMaxPostSize(boardType))?.map {
                                redisPost -> HomePostDto.of(redisPost, s3Service.getPreSignedUrlsFromS3Keys(redisPost.images))
                        } ?: listOf()
                    )
                }
            )
        }
        runBlocking { future.forEach { it.await() } }
        return future.map { it.getCompleted() }.sortedBy { it.boardId }
    }

    fun updateCacheByDeletedPost(post: PostEntity) {
        val (boardKey, cacheItems) = finCacheListByBoard(post.board)

        cacheItems?.find { it.postId == post.id }?.run {
            // delete할 item이 cache에 있는 경우 db에서 최신 4개 2개를 다시 fetch 해와 push 해준다
            // redis list에서 특정 index에 있는 아이템을 삭제하는 api가 보이지 않고,
            // db에서 2번째 혹은 4번째 최신 item을 db에서 가져오는거나 최신 2개 혹은 4개의 모든 item을 가져오는게 큰 차이가 없을 것 같아
            // 아래와 같이 4개를 새로 가져와 새로 redis로 올려줬다
            flushAndPushAllCache(
                boardKey,
                postRepository.findHomePostsByQuery(post.board.id, getMaxPostSize(post.board.type))
                    .map { RedisPostDto.of(it) }
            )
        }
    }

    fun updateCacheByUpdatedPost(post: PostEntity) {
        val (boardKey, cacheItems) = finCacheListByBoard(post.board)
        cacheItems?.forEachIndexed { index, redisPostDto ->
            if (redisPostDto.postId == post.id) {
                cacheItems[index] = RedisPostDto.of(post)
            }
        }
        flushAndPushAllCache(boardKey, cacheItems)
    }

    fun updateCacheByLikeOrReplyPost(post: PostEntity) {
        val (boardKey, cacheItems) = finCacheListByBoard(post.board)
        cacheItems?.find { it.postId == post.id }?.let {
            it.nlikes = post.nLikes
            it.nreplies = post.nReplies
        }
        flushAndPushAllCache(boardKey, cacheItems)
    }

    private fun flushAndPushAllCache(boardKey: String, cacheItems: List<RedisPostDto>?) {
        cacheItems?.let {
            redisPostTemplate.opsForList().operations.opsForList().leftPop(boardKey, cacheItems.size.toLong())
            redisPostTemplate.opsForList().operations.opsForList().rightPushAll(boardKey, it)
        }
    }

    private fun getBoardKey(board: BoardEntity) : String {
        return "board:${board.id}:${board.type.name}:${board.title}"
    }

    private fun parseBoardKey(boardKey: String) : Triple<Long, BoardType, String> {
        val parsed = boardKey.split(":")
        return Triple(parsed[1].toLong(), BoardType.valueOf(parsed[2]), parsed[3])
    }

    private fun getMaxPostSize(boardType: BoardType) : Long {
        return if (boardType.name.startsWith("CUSTOM")) 2 else 4
    }

    private fun finCacheListByBoard(board: BoardEntity) : Pair<String, MutableList<RedisPostDto>?> {
        val boardKey = getBoardKey(board)
        val cacheItems = redisPostTemplate.opsForList().operations.opsForList()
            .range(boardKey, 0, getMaxPostSize(board.type))
        return Pair(boardKey, cacheItems)
    }
}