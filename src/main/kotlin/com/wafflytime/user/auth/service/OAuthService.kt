package com.wafflytime.user.auth.service

import com.wafflytime.config.ExemptAuthentication
import com.wafflytime.exception.*
import com.wafflytime.user.auth.OAuthProperties
import com.wafflytime.user.auth.controller.dto.AuthToken
import com.wafflytime.user.auth.controller.dto.OAuthToken
import com.wafflytime.user.auth.controller.dto.SocialLoginRequest
import com.wafflytime.user.auth.controller.dto.SocialSignUpRequest
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime

interface OAuthService {
    fun getSocialEmail(providerName: String, code: String): String
    fun socialLogin(request: SocialLoginRequest): AuthToken
    fun socialSignUp(request: SocialSignUpRequest): AuthToken
}

@Service
class OAuthServiceImpl(
    private val oAuthProperties: OAuthProperties,
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
) : OAuthService {

    @ExemptAuthentication
    @Transactional
    override fun getSocialEmail(providerName: String, code: String): String {
        val provider = oAuthProperties.provider[providerName]
            ?: throw WafflyTime400("제공하지 않는 OAuth Provider 입니다.")
        val accessToken = getAccessToken(provider, code)
        return getSocialEmail(provider, providerName, accessToken)
    }

    @ExemptAuthentication
    @Transactional
    override fun socialLogin(request: SocialLoginRequest): AuthToken {
        val socialEmail = request.socialEmail
        val user = userRepository.findBySocialEmail(socialEmail)
            ?: throw WafflyTime404("존재하지 않는 이메일입니다.")
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    @ExemptAuthentication
    @Transactional
    override fun socialSignUp(request: SocialSignUpRequest): AuthToken {
        val socialEmail = request.socialEmail
        if (userRepository.findBySocialEmail(socialEmail) != null) {
            throw WafflyTime409("이미 이 social email로 가입한 계정이 존재합니다")
        }
        val univEmail = request.univEmail
        if (userRepository.findByUnivEmail(univEmail) != null) {
            throw WafflyTime409("이미 이 snu mail로 가입한 계정이 존재합니다")
        }
        val user = userRepository.save(UserEntity(socialEmail = socialEmail, univEmail = univEmail))
        return authTokenService.buildAuthToken(user, LocalDateTime.now())
    }

    private fun getAccessToken(provider: OAuthProperties.Provider, code: String): OAuthToken {
        return WebClient.create()
            .post()
            .uri(provider.tokenUri)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(tokenRequest(code, provider))
            .retrieve()
            .bodyToMono(OAuthToken::class.java)
            .block()
            ?: throw WafflyTime401("잘못된 code 입니다.")
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
            .bodyToMono<Map<String, Any>>()
            .block()
            ?: throw WafflyTime401("잘못된 token 입니다.")

        return when (providerName) {
            "google" -> google(attributes)
            "naver" -> naver(attributes)
            "kakao" -> kakao(attributes)
            "github" -> github(attributes)
            else -> throw WafflyTime400("제공하지 않는 소셜 로그인 서비스입니다.")
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