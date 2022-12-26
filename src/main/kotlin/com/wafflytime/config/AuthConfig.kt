package com.wafflytime.config

import com.wafflytime.service.SocialLoginService
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
class AuthConfig(
    private val socialLoginService: SocialLoginService,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain =
        httpSecurity
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // TODO 역할 별로 접근 권한 나눠야 함
            .authorizeHttpRequests()
                // 인증된 회원(ROLE_VERIFIED_USER)
                // 인증 안 된 회원(ROLE_UNVERIFIED_USER)
                // 비회원(ROLE_GUEST)
                .anyRequest().permitAll()
            .and()
            .oauth2Login()
                .userInfoEndpoint()
                .userService(socialLoginService)
                .and()
            .and()
            .logout()
                .clearAuthentication(true)
            .and()
            .build()
}