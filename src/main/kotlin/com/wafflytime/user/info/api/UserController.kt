package com.wafflytime.user.info.api

import com.wafflytime.config.UserIdFromToken
import com.wafflytime.post.dto.PostResponse
import com.wafflytime.user.info.api.dto.DeleteScrapResponse
import com.wafflytime.user.info.api.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.api.dto.UserInfo
import com.wafflytime.user.info.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/api/user/me")
    fun getMyInfo(@UserIdFromToken userId: Long): UserInfo {
        return userService.getUserInfo(userId)
    }

    @PutMapping("/api/user/me")
    fun updateMyInfo(
        @UserIdFromToken userId: Long,
        @Valid @RequestBody request: UpdateUserInfoRequest,
    ): UserInfo {
        return userService.updateUserInfo(userId, request)
    }

    @GetMapping("/api/user/myscrap")
    fun getMyScraps(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(userService.getMyScraps(userId, page, size))
    }

    @DeleteMapping("/api/user/myscrap")
    fun deleteScrap(
        @UserIdFromToken userId: Long,
        @RequestParam(required = true, value = "post") postId: Long
    ) : ResponseEntity<DeleteScrapResponse> {
        return ResponseEntity.ok(userService.deleteScrap(userId, postId))
    }

    @GetMapping("/api/user/mypost")
    fun getMyPosts(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ) : ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(userService.getMyPosts(userId, page, size))
    }

}