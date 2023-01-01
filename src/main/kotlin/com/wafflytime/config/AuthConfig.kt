package com.wafflytime.config

import com.wafflytime.user.auth.service.OAuth2SuccessHandler
import com.wafflytime.user.auth.service.SocialAuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter

@Configuration
@EnableWebSecurity
class AuthConfig(
    private val socialAuthService: SocialAuthService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain =
        httpSecurity
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // -- H2 console 테스트를 위한 설정 --
            .headers().addHeaderWriter(XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
            .and()
            // --------------------------------
            .authorizeHttpRequests()
                .anyRequest().permitAll()
            .and()
            .oauth2Login()
                .userInfoEndpoint()
                .userService(socialAuthService)
                .and()
                .successHandler(oAuth2SuccessHandler)
            .and()
            .build()
}