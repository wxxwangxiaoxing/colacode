package com.colacode.circle.config;

import com.colacode.circle.websocket.ChickenSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChickenSocketHandler chickenSocketHandler;

    public WebSocketConfig(ChickenSocketHandler chickenSocketHandler) {
        this.chickenSocketHandler = chickenSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chickenSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
