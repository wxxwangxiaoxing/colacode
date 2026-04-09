package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClickEventHandler implements WxMessageHandler {

    @Override
    public String getMsgType() {
        return "event";
    }

    @Override
    public String getEvent() {
        return "CLICK";
    }

    @Override
    public WxTextMessage handle(WxMessage wxMessage) {
        String eventKey = wxMessage.getEventKey();
        log.info("菜单点击事件, openId: {}, eventKey: {}", wxMessage.getFromUserName(), eventKey);

        if ("MENU_INTERVIEW".equals(eventKey)) {
            return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                    "欢迎使用 AI 模拟面试！请访问 https://colacode.com/interview 开始体验");
        }
        if ("MENU_PRACTICE".equals(eventKey)) {
            return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                    "欢迎使用刷题功能！请访问 https://colacode.com/subject 开始练习");
        }

        return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                "感谢您的点击，功能开发中...");
    }
}
