package com.wafflytime.user.info.service

import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.exception.WafflyTime409
import com.wafflytime.post.database.PostRepository
import com.wafflytime.post.database.ScrapRepository
import com.wafflytime.post.dto.PostResponse
import com.wafflytime.user.info.api.dto.DeleteScrapResponse
import com.wafflytime.user.info.api.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.api.dto.UserInfo
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.mail.api.dto.VerifyEmailRequest
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface UserService {
    fun getUser(userId: Long): UserEntity
    fun getUserInfo(userId: Long): UserInfo
    fun updateUserInfo(userId: Long, request: UpdateUserInfoRequest): UserInfo
    fun updateUserMailVerified(userId: Long, verifyEmailRequest: VerifyEmailRequest): UserEntity
    fun getMyScraps(userId: Long, page:Int, size:Int): List<PostResponse>
    fun deleteScrap(userId: Long, postId: Long): DeleteScrapResponse
    fun getMyPosts(userId: Long, page: Int, size: Int): List<PostResponse>
}

@Service
class UserServiceImpl (
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val scrapRepository: ScrapRepository,
    private val postRepository: PostRepository
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

    override fun getMyScraps(userId: Long, page:Int, size:Int): List<PostResponse> {
        return scrapRepository.findScrapsByUserId(userId, PageRequest.of(page, size)).map {
            PostResponse.of(it.post)
        }
    }

    @Transactional
    override fun deleteScrap(userId: Long, postId: Long): DeleteScrapResponse {
        val scrap = scrapRepository.findByPostIdAndUserId(postId, userId) ?: throw WafflyTime404("존재하지 않는 스크랩 입니다")
        if (userId != scrap.user.id) throw WafflyTime401("스크랩한 유저만 스크랩을 삭제할 수 있습니다")
        scrap.post.nScraps--
        scrapRepository.delete(scrap)

        // 삭제하면 프론트에서 알아서 삭제한 포스트는 보여주지 않는다고 가정(프론트가 구현)
        // 프론트에서 요청하면 DeleteScrapResponse가 아닌 getMyScraps의 리턴값처럼 삭제된 포스트를 반영해 다시 리턴해줘도 됨
        return DeleteScrapResponse(scrap.post.id)
    }

    override fun getMyPosts(userId: Long, page: Int, size: Int): List<PostResponse> {
        return postRepository.findAllByWriterId(
            userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).content.map {
            PostResponse.of(it)
        }
    }

    private fun getUserById(userId: Long): UserEntity {
        return userRepository.findByIdOrNull(userId)
            ?: throw WafflyTime404("해당 유저 id를 찾을 수 없습니다")
    }
}