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

    // TODO: user entity column ??? ???????????? ?????? ?????? ??????
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
        // ??? update api??? nickname??? password??? ???????????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????, preSignedUrl??? null??? ?????????
        // ??????????????? ???????????? ?????? ????????? ?????? response??? ????????? ????????? ???????????? presSingedUrl null ?????? ???????????? ?????? ???????????? ???????????? ?????? ?????? ????????? ??????????????? ??????
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

        // ???????????? ??????????????? ????????? ????????? ???????????? ???????????? ???????????? ??????(???????????? ??????)
        // ??????????????? ???????????? DeleteScrapResponse??? ?????? getMyScraps??? ??????????????? ????????? ???????????? ????????? ?????? ??????????????? ???
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
        // ????????? ????????? ???????????? ????????? ?????? ??????
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