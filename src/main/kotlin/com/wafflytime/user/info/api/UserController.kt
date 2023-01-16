package com.wafflytime.user.info.api

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.config.ExemptEmailVerification
import com.wafflytime.config.UserIdFromToken
import com.wafflytime.post.dto.PostResponse
import com.wafflytime.user.info.dto.DeleteScrapResponse
import com.wafflytime.user.info.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.dto.UploadProfileImageRequest
import com.wafflytime.user.info.dto.UserInfo
import com.wafflytime.user.info.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
) {

    @ExemptEmailVerification
    @GetMapping("/api/user/me")
    fun getMyInfo(@UserIdFromToken userId: Long): UserInfo {
        return userService.getUserInfo(userId)
    }

    @ExemptAuthentication
    @GetMapping("/api/user/check/id/{id}")
    fun checkLoginIdConflict(@PathVariable id: String): String {
        userService.checkLoginIdConflict(id)
        return "사용 가능한 아이디입니다"
    }

    @ExemptAuthentication
    @GetMapping("/api/user/check/nickname/{nickname}")
    fun checkNicknameConflict(@PathVariable nickname: String): String {
        userService.checkNicknameConflict(nickname)
        return "사용 가능한 닉네임입니다"
    }

    @PutMapping("/api/user/me")
    fun updateMyInfo(
        @UserIdFromToken userId: Long,
        @Valid @RequestBody request: UpdateUserInfoRequest,
    ): UserInfo {
        return userService.updateUserInfo(userId, request)
    }

    @PutMapping("/api/user/me/profile")
    fun updateProfileImage(
        @UserIdFromToken userId: Long,
        @Valid @RequestBody request: UploadProfileImageRequest,
    ) : ResponseEntity<UserInfo> {
        return ResponseEntity.ok(userService.updateProfileImage(userId, request))
    }

    @DeleteMapping("/api/user/me/profile")
    fun updateProfileImage(
        @UserIdFromToken userId: Long
    ) : ResponseEntity<UserInfo> {
        return ResponseEntity.ok(userService.deleteProfileImage(userId))
    }

    @GetMapping("/api/user/myscrap")
    fun getMyScraps(
        @UserIdFromToken userId: Long,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostResponse>> {
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
    ) : ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(userService.getMyPosts(userId, page, size))
    }
}