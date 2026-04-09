package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnsubscribeEventHandler implements WxMessageHandler {

    @Override
    public String getMsgType() {
        return "event";
    }

    @Override
    public String getEvent() {
        return "unsubscribe";
    }

    @Override
    public WxTextMessage handle(WxMessage wxMessage) {
        log.info("用户取关, openId: {}", wxMessage.getFromUserName());
        return null;
    }
}
