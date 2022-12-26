package com.wafflytime.service

import com.wafflytime.database.SocialEntity
import com.wafflytime.database.SocialRepository
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
class SocialLoginService(
    private val socialRepository: SocialRepository,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        userRequest ?: throw Exception()
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
        val email = attributes["email"] as String
        val social = socialRepository.findByProviderAndEmail(
            provider = registrationId, email = email
        ) ?: socialRepository.save(SocialEntity(provider = registrationId, email = email))

        val role = social.user?.univEmail?.let {
            "ROLE_VERIFIED_USER"
        } ?: "ROLE_UNVERIFIED_USER"

        return DefaultOAuth2User(
            Collections.singleton(SimpleGrantedAuthority(role)),
            attributes,
            "email",
        )
    }
}