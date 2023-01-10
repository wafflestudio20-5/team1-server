package com.wafflytime.config

import com.wafflytime.user.auth.exception.AuthTokenNotProvided
import com.wafflytime.user.auth.exception.MailNotVerified
import com.wafflytime.user.auth.exception.WrongUsageUserIdFromToken
import com.wafflytime.user.auth.service.AuthTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

// 로그인 상태를 요구하는 요청에서 userId parameter 에 사용
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UserIdFromToken

// 로그인 상태를 요구하지 않는 요청에 사용
@Target(AnnotationTarget.FUNCTION)
annotation class ExemptAuthentication
// 이메일 인증을 요구하지 않는 요청에 사용
@Target(AnnotationTarget.FUNCTION)
annotation class ExemptEmailVerification


@Configuration
class WebMvcConfiguration(
    private val localAuthArgumentResolver: LocalAuthArgumentResolver,
    private val localAuthInterceptor: LocalAuthInterceptor,
): WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(localAuthArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localAuthInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .maxAge(3000)
    }

}

@Configuration
class LocalAuthArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(UserIdFromToken::class.java) &&
                parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        if (supportsParameter(parameter)) {
            val httpServletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: throw WrongUsageUserIdFromToken
            return httpServletRequest.getAttribute("UserIdFromToken")
        } else {
            throw WrongUsageUserIdFromToken
        }
    }

}

@Configuration
class LocalAuthInterceptor(
    private val authTokenService: AuthTokenService,
): HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!request.requestURI.startsWith("/v3/api-docs")) {

            val handlerCasted = handler as? HandlerMethod ?: return true


            if (!handlerCasted.hasMethodAnnotation(ExemptAuthentication::class.java)) {
                val accessToken = request.getHeader("Authorization") ?: throw AuthTokenNotProvided

                val authResult = authTokenService.authenticate(accessToken)
                request.setAttribute("UserIdFromToken", authTokenService.getUserId(authResult))

                if (!handlerCasted.hasMethodAnnotation(ExemptEmailVerification::class.java)) {
                    if (!authTokenService.isEmailVerified(authResult)) {
                        throw MailNotVerified
                    }
                }
            }

        }

        return super.preHandle(request, response, handler)
    }

}