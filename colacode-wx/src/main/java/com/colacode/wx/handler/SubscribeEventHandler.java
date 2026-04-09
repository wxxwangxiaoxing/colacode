package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubscribeEventHandler implements WxMessageHandler {

    @Override
    public String getMsgType() {
        return "event";
    }

    @Override
    public String getEvent() {
        return "subscribe";
    }

    @Override
    public WxTextMessage handle(WxMessage wxMessage) {
        log.info("用户关注, openId: {}", wxMessage.getFromUserName());
        return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                "欢迎关注 ColaCode 程序员社区！\n" +
                "🎯 回复【面试】开始 AI 模拟面试\n" +
                "📚 回复【刷题】获取每日推荐题目\n" +
                "💬 回复【帮助】查看更多功能");
    }
}
