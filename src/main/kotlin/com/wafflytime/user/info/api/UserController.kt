package com.wafflytime.user.info.api

import com.wafflytime.config.UserIdFromToken
import com.wafflytime.user.info.api.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.api.dto.UserInfo
import com.wafflytime.user.info.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

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

}