package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultMessageHandler implements WxMessageHandler {

    @Override
    public String getMsgType() {
        return null;
    }

    @Override
    public String getEvent() {
        return null;
    }

    @Override
    public WxTextMessage handle(WxMessage wxMessage) {
        log.info("未知消息类型, msgType: {}, event: {}", wxMessage.getMsgType(), wxMessage.getEvent());
        return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                "暂不支持该消息类型，敬请期待！");
    }
}
