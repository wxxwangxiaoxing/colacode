package com.colacode.circle.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChickenSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.put(userId, session);
            log.info("用户 {} 建立 WebSocket 连接", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("收到用户 {} 的消息: {}", userId, message.getPayload());

        String payload = message.getPayload();
        for (Map.Entry<Long, WebSocketSession> entry : SESSION_MAP.entrySet()) {
            if (!entry.getKey().equals(userId)) {
                try {
                    entry.getValue().sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("用户 {} 断开 WebSocket 连接", userId);
        }
    }

    public void sendMessage(Long userId, String message) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败, userId: {}", userId, e);
            }
        }
    }
}
