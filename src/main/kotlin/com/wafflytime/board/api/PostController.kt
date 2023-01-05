package com.wafflytime.board.api

import com.wafflytime.board.dto.*
import com.wafflytime.board.service.PostService
import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.s3.service.S3Service
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PostController(
    private val postService: PostService,
    private val s3Service: S3Service,
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
    @PutMapping("/api/board/{boardId}/post/{postId}")
    fun updatePost(
        @UserIdFromToken userId: Long,
        @PathVariable boardId: Long,
        @PathVariable postId: Long,
        @Valid @RequestBody request: UpdatePostRequest
    ) : ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.updatePost(userId, boardId, postId, request))
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

    @ExemptAuthentication
    @PostMapping("/api/board/{boardId}/post-photo")
    fun createPostPhoto(
        @PathVariable boardId: Long,
        @Valid request: CreatePostVO,
    ) : ResponseEntity<String> {
        val fileUrl = s3Service.uploadFile(request.files!![0])

        println("request: ${request}")
        println("fileUrl: ${fileUrl}")
        return ResponseEntity.ok("hello")
    }

}