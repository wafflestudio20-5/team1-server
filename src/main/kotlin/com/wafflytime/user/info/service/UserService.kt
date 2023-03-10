package com.wafflytime.user.info.service

import com.wafflytime.common.CursorPage
import com.wafflytime.common.S3Service
import com.wafflytime.post.database.PostRepository
import com.wafflytime.post.database.ScrapRepository
import com.wafflytime.post.dto.PostResponse
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.info.dto.DeleteScrapResponse
import com.wafflytime.user.info.dto.UpdateUserInfoRequest
import com.wafflytime.user.info.dto.UploadProfileImageRequest
import com.wafflytime.user.info.dto.UserInfo
import com.wafflytime.user.info.exception.*
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface UserService {
    fun getUser(userId: Long): UserEntity
    fun getUserInfo(userId: Long): UserInfo
    fun checkLoginIdConflict(loginId: String)
    fun checkNicknameConflict(nickname: String)
    fun checkUnivEmailConflict(univEmail: String)
    fun updateUserInfo(userId: Long, request: UpdateUserInfoRequest): UserInfo
    fun updateUserMailVerified(userId: Long, email: String): UserEntity
    fun getMyScraps(userId: Long, page: Long, size: Long): CursorPage<PostResponse>
    fun getMyScraps(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse>
    fun deleteScrap(userId: Long, postId: Long): DeleteScrapResponse
    fun getMyPosts(userId: Long, page: Long, size: Long): CursorPage<PostResponse>
    fun getMyPosts(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse>
    fun updateProfileImage(userId: Long, request: UploadProfileImageRequest): UserInfo
    fun deleteProfileImage(userId: Long): UserInfo
    fun getMyRepliedPosts(userId: Long, page: Long, size: Long): CursorPage<PostResponse>
    fun getMyRepliedPosts(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse>
}

@Service
class UserServiceImpl (
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val scrapRepository: ScrapRepository,
    private val postRepository: PostRepository,
    private val s3Service: S3Service
) : UserService {

    @Transactional
    override fun getUser(userId: Long): UserEntity {
        return getUserById(userId)
    }

    // TODO: user entity column 이 늘어남에 따라 추가 필요
    @Transactional
    override fun getUserInfo(userId: Long): UserInfo {
        val user = getUserById(userId)
        return UserInfo.of(user, preSignedUrl = s3Service.getPreSignedUrlFromS3Key(user.profileImage))
    }

    @Transactional
    override fun checkLoginIdConflict(loginId: String) {
        userRepository.findByLoginId(loginId)?.let { throw LoginIdConflict }
    }

    @Transactional
    override fun checkNicknameConflict(nickname: String) {
        val len = nickname.length
        if (len < 2 || 10 < len) throw InvalidNicknameLength
        userRepository.findByNickname(nickname)?.let { throw NicknameConflict }
    }

    @Transactional
    override fun checkUnivEmailConflict(univEmail: String) {
        userRepository.findByUnivEmail(univEmail)?.let { throw MailConflict }
    }

    override fun updateUserInfo(userId: Long, request: UpdateUserInfoRequest): UserInfo {
        val user = getUserById(userId)
        request.run {
            if ((oldPassword == null) != (newPassword == null)) throw InsufficientPasswordUpdateInfo

            try {
                user.update(
                    oldPassword?.run {
                        if (passwordEncoder.matches(this, user.password))
                            newPassword?.let { passwordEncoder.encode(it) }
                        else
                            throw PasswordMismatch
                    },
                    nickname,
                )

                userRepository.save(user)
            } catch (e: DataIntegrityViolationException) {
                throw NicknameConflict
            }
        }
        // 이 update api는 nickname과 password만 업데이트 하기 때문에 유저가 프로필 사진이 있다고 하더라도, preSignedUrl이 null로 내려감
        // 프론트에게 업데이트 하는 경우에 받은 response는 수정한 내용만 반영하고 presSingedUrl null 인건 무시하고 이미 유저에게 보여주고 있는 사진 그대로 보여주기를 요청
        return UserInfo.of(user)
    }

    @Transactional
    override fun updateUserMailVerified(userId: Long, email: String): UserEntity {
        val user = getUserById(userId)
        userRepository.findByUnivEmail(email)?.let { throw MailConflict }
        user.univEmail = email

        return user
    }

    override fun getMyScraps(userId: Long, page: Long, size: Long): CursorPage<PostResponse> {
        return scrapRepository.findScrapsByUserId(userId, page, size).map {
            PostResponse.of(userId, it.post, s3Service.getPreSignedUrlsFromS3Keys(it.post.images))
        }
    }

    override fun getMyScraps(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return scrapRepository.findScrapsByUserId(userId, cursor, size).map {
            PostResponse.of(userId, it.post, s3Service.getPreSignedUrlsFromS3Keys(it.post.images))
        }
    }

    @Transactional
    override fun deleteScrap(userId: Long, postId: Long): DeleteScrapResponse {
        val scrap = scrapRepository.findByPostIdAndUserId(postId, userId) ?: throw NotScrapped
        scrap.post.nScraps--
        scrapRepository.delete(scrap)

        // 삭제하면 프론트에서 알아서 삭제한 포스트는 보여주지 않는다고 가정(프론트가 구현)
        // 프론트에서 요청하면 DeleteScrapResponse가 아닌 getMyScraps의 리턴값처럼 삭제된 포스트를 반영해 다시 리턴해줘도 됨
        return DeleteScrapResponse(scrap.post.id)
    }

    override fun getMyPosts(userId: Long, page: Long, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByWriterId(userId, page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    override fun getMyPosts(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByWriterId(userId, cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    override fun getMyRepliedPosts(userId: Long, page: Long, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByUserReply(userId, page, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    override fun getMyRepliedPosts(userId: Long, cursor: Long?, size: Long): CursorPage<PostResponse> {
        return postRepository.findAllByUserReply(userId, cursor, size).map {
            PostResponse.of(userId, it, s3Service.getPreSignedUrlsFromS3Keys(it.images))
        }
    }

    @Transactional
    override fun updateProfileImage(userId: Long, request: UploadProfileImageRequest): UserInfo {
        val user = getUserById(userId)
        // 기존에 저장된 이미지가 존재할 경우 삭제
        user.profileImage?.let { s3Service.deleteFile(it) }

        val s3ImageUrlDto = s3Service.gerPreSignedUrlAndS3Url(request.fileName)
        user.profileImage = s3ImageUrlDto.s3Url
        return UserInfo.of(user, s3ImageUrlDto.preSignedUrl)
    }

    @Transactional
    override fun deleteProfileImage(userId: Long): UserInfo {
        val user = getUserById(userId)
        val s3FileKey = user.profileImage
        s3Service.deleteFile(s3FileKey)
        user.profileImage = null
        return UserInfo.of(user)
    }

    private fun getUserById(userId: Long): UserEntity {
        return userRepository.findByIdOrNull(userId)
            ?: throw UserNotFound
    }
}