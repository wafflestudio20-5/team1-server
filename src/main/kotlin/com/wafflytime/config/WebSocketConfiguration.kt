package com.wafflytime.config

import com.wafflytime.chat.service.WebSocketService
import com.wafflytime.user.auth.exception.AuthTokenNotProvided
import com.wafflytime.user.auth.service.AuthTokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfiguration(
    private val wafflytimeWebSocketHandler: WafflytimeWebSocketHandler,
    private val webSocketHandshakeInterceptor: WebSocketHandshakeInterceptor,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler(), "/api/ws-connect")
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOrigins("*")
    }

    @Bean
    fun handler() = wafflytimeWebSocketHandler

}

@Component
class WafflytimeWebSocketHandler(
    private val webSocketService: WebSocketService
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        webSocketService.addSession(session)
        super.afterConnectionEstablished(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        webSocketService.sendMessage(session, message)
        super.handleTextMessage(session, message)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        webSocketService.removeSession(session)
        super.afterConnectionClosed(session, status)
    }

}

@Configuration
class WebSocketHandshakeInterceptor(
    private val authTokenService: AuthTokenService,
) : HttpSessionHandshakeInterceptor() {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val accessToken = request.headers["Authorization"]?.first() ?: throw AuthTokenNotProvided

        val authResult = authTokenService.authenticate(accessToken)
        attributes["UserIdFromToken"] = authTokenService.getUserId(authResult)
        attributes["JwtExpiration"] = authTokenService.getExpiration(authResult)

        return super.beforeHandshake(request, response, wsHandler, attributes)
    }

}
