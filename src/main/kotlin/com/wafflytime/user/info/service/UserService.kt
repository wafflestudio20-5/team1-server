package com.wafflytime.user.info.service

import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.mail.api.dto.VerifyEmailRequest
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.user.info.api.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.api.dto.UserInfo
import com.wafflytime.exception.WafflyTime409
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface UserService {
    fun getUser(userId: Long): UserEntity
    fun getUserInfo(userId: Long): UserInfo
    fun updateUserInfo(userId: Long, request: UpdateUserInfoRequest): UserInfo
    fun updateUserMailVerified(userId: Long, verifyEmailRequest: VerifyEmailRequest): UserEntity
}

@Service
class UserServiceImpl (
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
) : UserService {

    @Transactional
    override fun getUser(userId: Long): UserEntity {
        return getUserById(userId)
    }

    // TODO: user entity column 이 늘어남에 따라 추가 필요
    @Transactional
    override fun getUserInfo(userId: Long): UserInfo {
        val user = getUserById(userId)
        return UserInfo.of(user)
    }

    @Transactional
    override fun updateUserInfo(userId: Long, request: UpdateUserInfoRequest): UserInfo {
        val user = getUserById(userId)
        request.run {
            user.update(
                password?.let { passwordEncoder.encode(it) },
                nickname,
            )
        }
        return UserInfo.of(user)
    }

    @Transactional
    override fun updateUserMailVerified(userId: Long, verifyEmailRequest: VerifyEmailRequest): UserEntity {
        val user = getUserById(userId)
        userRepository.findByUnivEmail(verifyEmailRequest.email)?.let { throw WafflyTime409("이미 이 snu mail로 가입한 계정이 존재합니다") }
        user.univEmail = verifyEmailRequest.email

        return user
    }

    private fun getUserById(userId: Long): UserEntity {
        return userRepository.findByIdOrNull(userId)
            ?: throw WafflyTime404("해당 유저 id를 찾을 수 없습니다")
    }
}