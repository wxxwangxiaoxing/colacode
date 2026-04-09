package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TextMessageHandler implements WxMessageHandler {

    @Override
    public String getMsgType() {
        return "text";
    }

    @Override
    public String getEvent() {
        return null;
    }

    @Override
    public WxTextMessage handle(WxMessage wxMessage) {
        log.info("收到文本消息, fromUser: {}, content: {}", wxMessage.getFromUserName(), wxMessage.getContent());
        String content = wxMessage.getContent();

        if ("帮助".equals(content)) {
            return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                    "欢迎使用 ColaCode 公众号！\n回复【面试】开始模拟面试\n回复【刷题】获取今日推荐题目\n回复【帮助】查看帮助信息");
        }
        if ("面试".equals(content)) {
            return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                    "模拟面试功能开发中，敬请期待！");
        }
        if ("刷题".equals(content)) {
            return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                    "今日推荐题目：Java 中 HashMap 的底层原理是什么？\n回复【答案】查看解析");
        }

        return new WxTextMessage(wxMessage.getFromUserName(), wxMessage.getToUserName(),
                "收到您的消息: " + content + "\n回复【帮助】查看可用功能");
    }
}
