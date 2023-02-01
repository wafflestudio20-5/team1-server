package com.wafflytime.user.auth.exception

import com.wafflytime.exception.AuthException
import org.springframework.http.HttpStatus

open class Auth400(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Auth401(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Auth403(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Auth404(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Auth409(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.CONFLICT)

open class Auth500(msg: String, errorCode: Int) : AuthException(msg, errorCode, HttpStatus.INTERNAL_SERVER_ERROR)

object WrongUsageUserIdFromToken : Auth500("잘못된 `@UserIdFromToken` 어노테이션 사용", 0)
object AuthTokenNotProvided : Auth401("로그인 후 이용 가능합니다", 1)
object MailNotVerified : Auth403("학교 이메일 인증 후 이용 가능합니다", 2)
object AuthTokenExpired : Auth401("만료된 토큰입니다", 3)
object InvalidAuthToken : Auth401("잘못된 인증입니다", 4)
object RefreshTokenTakenOver : Auth409("Refresh token 탈취 의심됨", 5)
object SignUpConflict : Auth409("회원가입 중복 정보 존재", 6)
object LoginFailure : Auth400("존재하지 않는 아이디이거나 비밀번호가 잘못되었습니다", 7)
object SocialEmailConflict : Auth409("이미 사용중인 소셜 이메일입니다", 8)
object SocialLoginFailure : Auth404("해당 소셜 이메일로 가입한 계정이 존재하지 않습니다", 9)
object OAuthProviderNotSupported : Auth400("지원하지 않는 OAuth Provider 입니다", 10)
object InvalidAuthorizationCode : Auth401("잘못된 Authorization code 입니다", 11)
object InvalidOAuthToken : Auth500("잘못된 OAuth access token", 12)
object OAuthCodeExpired : Auth401("만료된 코드입니다", 13)