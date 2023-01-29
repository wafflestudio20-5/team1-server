package com.wafflytime.post.api

import com.wafflytime.board.dto.*
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.post.dto.*
import com.wafflytime.post.service.PostService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PostController(
    private val postService: PostService
) {

    @GetMapping("/api/board/{boardId}/post/{postId}")
    fun getPost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.getPost(userId, boardId, postId))
    }

    @GetMapping("/api/board/{boardId}/posts")
    fun getPosts(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.getPosts(userId, boardId, page, size))
    }

    @PostMapping("/api/board/{boardId}/post")
    fun createPost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @Valid @RequestBody request: CreatePostRequest,
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.createPost(userId, boardId, request))
    }

    @PutMapping("/api/board/{boardId}/post/{postId}")
    fun updatePost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @Valid @RequestBody request: UpdatePostRequest
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.updatePost(userId, boardId, postId, request))
    }

    @DeleteMapping("/api/board/{boardId}/post/{postId}")
    fun deletePost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ) : ResponseEntity<DeletePostResponse> {
        return ResponseEntity.ok(postService.deletePost(userId, boardId, postId))
    }

    // 에타에 좋아요 취소는 없음
    @PostMapping("/api/board/{boardId}/post/{postId}/like")
    fun likePost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.likePost(userId, boardId, postId))
    }

    @PostMapping("/api/board/{boardId}/post/{postId}/scrap")
    fun scrapPost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.scrapPost(userId, boardId, postId))
    }

    @GetMapping("/api/hotpost")
    fun getHotPost(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.getHostPosts(userId, page, size))
    }

    @GetMapping("/api/bestpost")
    fun getBestPost(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.getBestPosts(userId, page, size))
    }

    @GetMapping("/api/posts/search")
    fun searchBoards(
        @UserIdFromToken userId: Long,
        @RequestParam(required = true, value = "keyword") keyword: String,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.searchPosts(userId, keyword, page, size))
    }

    @GetMapping("/api/homeposts")
    fun getHomePostTest() : ResponseEntity<List<HomePostResponse>> {
        return ResponseEntity.ok(postService.getHomePostsTest())
    }

    @GetMapping("/api/latestposts")
    fun getLatestPostsByCategory(
        @UserIdFromToken userId: Long,
        @RequestParam(required = true, value = "category") category: BoardCategory,
        @RequestParam(required = false, value = "size", defaultValue = "2") size: Int
    ) : ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(postService.getLatestPostsByCategory(userId, category, size))
    }
}