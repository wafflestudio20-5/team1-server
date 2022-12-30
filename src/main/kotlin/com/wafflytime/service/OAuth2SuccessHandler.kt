package com.wafflytime.service

import com.wafflytime.database.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val userRepository: UserRepository,
) : AuthenticationSuccessHandler {
    private val redirectStrategy = DefaultRedirectStrategy()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val oAuth2User = authentication?.principal as OAuth2User
        val socialEmail = oAuth2User.name

        // 임시로 설정한 accessToken
        val accessToken = "accessToken"

        // 임시로 설정한 redirectUrl
        val redirectUrl =
            when (userRepository.findBySocialEmail(socialEmail)) {
                null -> {
                    // accessToken = socialEmail 만 담고 있는 accessToken
                    "/signup" // 닉네임 설정 및 학교 메일 인증 페이지로
                }
                else -> {
                    // accessToken = user 에 대한 accessToken
                    "/home" // 유저 로그인 완료시 가는 페이지로
                }
            }

        val targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
            .queryParam("accessToken", accessToken)
            .build()
            .toUriString()
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}