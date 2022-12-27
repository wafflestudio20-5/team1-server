package com.wafflytime.service

import com.wafflytime.database.UserEntity
import com.wafflytime.database.UserRepository
import com.wafflytime.dto.VerifyEmailRequest
import com.wafflytime.exception.WafflyTime404
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
) {

    @Transactional
    fun updateUserMailVerified(userId: Long, verifyEmailRequest: VerifyEmailRequest) {
        val user: UserEntity = userRepository.findByIdOrNull(userId)
            ?: throw WafflyTime404("해당 유저 id를 찾을 수 없습니다")
        user.univEmail = verifyEmailRequest.email
    }
}