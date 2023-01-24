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
}