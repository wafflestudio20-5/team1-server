package com.wafflytime.user.auth.service

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.user.auth.dto.AuthToken
import com.wafflytime.user.auth.dto.OAuthToken
import com.wafflytime.user.auth.dto.SocialSignUpRequest
import com.wafflytime.user.auth.exception.*
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.info.exception.NicknameConflict
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime

interface OAuthService {
    fun socialLogin(providerName: String, code: String): AuthToken
    fun socialSignUp(providerName: String, code: String, request: SocialSignUpRequest): AuthToken
}

@Service
class OAuthServiceImpl(
    private val oAuthProperties: OAuthProperties,
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
) : OAuthService {

    @ExemptAuthentication
    @Transactional
    override fun socialLogin(providerName: String, code: String): AuthToken {
        val socialEmail = getSocialEmail(providerName, code)
        val user = userRepository.findBySocialEmail(socialEmail)
            ?: signUp(socialEmail, "temp-nickname-$socialEmail")
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    @ExemptAuthentication
    @Transactional
    override fun socialSignUp(providerName: String, code: String, request: SocialSignUpRequest): AuthToken {
        val socialEmail = getSocialEmail(providerName, code)
        val user = signUp(socialEmail, request.nickname)
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    private fun signUp(socialEmail: String, nickname: String): UserEntity {
        if (userRepository.findBySocialEmail(socialEmail) != null) {
            throw SocialEmailConflict
        }
        return try {
            userRepository.save(UserEntity(socialEmail = socialEmail, nickname = nickname))
        } catch (e: DataIntegrityViolationException) {
            throw NicknameConflict
        }
    }

    fun getSocialEmail(providerName: String, code: String): String {
        val provider = oAuthProperties.provider[providerName]
            ?: throw OAuthProviderNotSupported
        val accessToken = getAccessToken(provider, code)
        return getSocialEmail(provider, providerName, accessToken)
    }

    private fun getAccessToken(provider: OAuthProperties.Provider, code: String): OAuthToken {
        return WebClient.create()
            .post()
            .uri(provider.tokenUri)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(tokenRequest(code, provider))
            .retrieve()
            .onStatus({ it.isError }, { throw InvalidAuthorizationCode })
            .bodyToMono(OAuthToken::class.java)
            .block()
            ?: throw InvalidAuthorizationCode
    }

    private fun tokenRequest(
        code: String,
        provider: OAuthProperties.Provider,
    ): MultiValueMap<String, String> {
        val body = LinkedMultiValueMap<String, String>()
        body["client_id"] = provider.clientId
        body["client_secret"] = provider.clientSecret
        body["code"] = code
        body["grant_type"] = "authorization_code"
        body["redirect_uri"] = provider.redirectUri
        return body
    }

    private fun getSocialEmail(
        provider: OAuthProperties.Provider,
        providerName: String,
        accessToken: OAuthToken
    ): String {
        val attributes = WebClient.create()
            .get()
            .uri(provider.userInfoUri)
            .accept(MediaType.APPLICATION_JSON)
            .headers { header -> header.setBearerAuth(accessToken.accessToken) }
            .retrieve()
            .onStatus({ it.isError }, { throw InvalidOAuthToken })
            .bodyToMono<Map<String, Any>>()
            .block()
            ?: throw InvalidOAuthToken

        return when (providerName) {
            "google" -> google(attributes)
            "naver" -> naver(attributes)
            "kakao" -> kakao(attributes)
            "github" -> github(attributes)
            else -> throw OAuthProviderNotSupported
        }
    }

    private fun google(attributes: Map<String, Any>): String {
        return attributes["email"] as String
    }

    private fun naver(attributes: Map<String, Any>): String {
        val response = attributes["response"] as Map<String, Any>
        return response["email"] as String
    }

    private fun kakao(attributes: Map<String, Any>): String {
        val kakaoAccount = attributes["kakao_account"] as Map<String, Any>
        return kakaoAccount["email"] as String
    }

    private fun github(attributes: Map<String, Any>): String {
        return attributes["email"] as String
    }
}