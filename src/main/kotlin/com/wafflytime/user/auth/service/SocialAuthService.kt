package com.wafflytime.user.auth.service

import com.wafflytime.exception.WafflyTime400
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*

@Service
class SocialAuthService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        userRequest ?: throw WafflyTime400("잘못된 로그인 요청입니다.")
        val oAuth2UserService = DefaultOAuth2UserService()
        val oAuth2User = oAuth2UserService.loadUser(userRequest)
        val registrationId = userRequest
            .clientRegistration
            .registrationId

        val userNameAttributeName = userRequest
            .clientRegistration
            .providerDetails
            .userInfoEndpoint
            .userNameAttributeName

        val attributes =
            OAuth2Attribute.of(provider = registrationId, attributeKey = userNameAttributeName, oAuth2User.attributes)
                .toMap()

        return DefaultOAuth2User(
            Collections.singleton(SimpleGrantedAuthority("ROLE_GUEST")),
            attributes,
            "email",
        )
    }
}