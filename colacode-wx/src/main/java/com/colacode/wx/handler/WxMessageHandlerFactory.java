package com.colacode.wx.handler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WxMessageHandlerFactory {

    private final List<WxMessageHandler> handlerList;

    private WxMessageHandler defaultHandler;

    private final java.util.Map<String, WxMessageHandler> handlerMap = new java.util.HashMap<>();

    public WxMessageHandlerFactory(List<WxMessageHandler> handlerList) {
        this.handlerList = handlerList;
        for (WxMessageHandler handler : handlerList) {
            if (handler.getMsgType() == null && handler.getEvent() == null) {
                defaultHandler = handler;
                continue;
            }
            String key = buildKey(handler.getMsgType(), handler.getEvent());
            handlerMap.put(key, handler);
        }
    }

    public WxMessageHandler getHandler(String msgType, String event) {
        String key = buildKey(msgType, event);
        WxMessageHandler handler = handlerMap.get(key);
        return handler != null ? handler : defaultHandler;
    }

    private String buildKey(String msgType, String event) {
        return msgType + ":" + (event != null ? event : "");
    }
}
