package com.wafflytime.board.controller

import com.wafflytime.board.dto.CreatePostRequest
import com.wafflytime.board.dto.PostResponse
import com.wafflytime.board.service.PostService
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(
    private val postService: PostService
) {
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
    ) : ResponseEntity<List<PostResponse>> {
        // Pageable 하게 구현하자!
        return ResponseEntity.ok(postService.getPosts(boardId))
    }
}