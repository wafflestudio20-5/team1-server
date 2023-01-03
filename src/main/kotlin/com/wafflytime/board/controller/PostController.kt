package com.wafflytime.board.controller

import com.wafflytime.board.dto.CreatePostRequest
import com.wafflytime.board.dto.DeletePostResponse
import com.wafflytime.board.dto.PostResponse
import com.wafflytime.board.service.PostService
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PostController(
    private val postService: PostService
) {

    @ExemptEmailVerification
    @GetMapping("/api/board/{boardId}/post/{postId}")
    fun getPost(
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.getPost(boardId, postId))
    }

    @ExemptEmailVerification
    @GetMapping("/api/board/{boardId}/posts")
    fun getPosts(
        @PathVariable boardId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.getPosts(boardId, page, size))
    }

    @ExemptEmailVerification
    @PostMapping("/api/board/{boardId}/post")
    fun createPost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @Valid @RequestBody request: CreatePostRequest
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.createPost(userId, boardId, request))
    }

    @ExemptEmailVerification
    @DeleteMapping("/api/board/{boardId}/post/{postId}")
    fun deletePost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long
    ) : ResponseEntity<DeletePostResponse> {
        return ResponseEntity.ok(postService.deletePost(userId, boardId, postId))
    }

}