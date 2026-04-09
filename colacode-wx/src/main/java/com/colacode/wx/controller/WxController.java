package com.colacode.wx.controller;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import com.colacode.wx.handler.WxMessageHandler;
import com.colacode.wx.handler.WxMessageHandlerFactory;
import com.colacode.wx.util.WxSignatureUtil;
import com.colacode.wx.util.WxXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/wx")
public class WxController {

    @Value("${wx.mp.token:colacode}")
    private String token;

    private final WxMessageHandlerFactory handlerFactory;

    public WxController(WxMessageHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @GetMapping(produces = "text/plain;charset=utf-8")
    public String verify(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {
        log.info("微信服务器验证: signature={}, timestamp={}, nonce={}, echostr={}",
                signature, timestamp, nonce, echostr);

        if (WxSignatureUtil.checkSignature(token, signature, timestamp, nonce)) {
            log.info("微信服务器验证成功");
            return echostr;
        }
        log.warn("微信服务器验证失败");
        return "";
    }

    @PostMapping(produces = "application/xml;charset=utf-8")
    public String handleMessage(@RequestBody String xmlData) {
        log.info("收到微信消息: {}", xmlData);

        WxMessage wxMessage = WxXmlUtil.xmlToMessage(xmlData);
        WxMessageHandler handler = handlerFactory.getHandler(wxMessage.getMsgType(), wxMessage.getEvent());
        WxTextMessage response = handler.handle(wxMessage);

        if (response == null) {
            return "success";
        }

        String responseXml = WxXmlUtil.messageToXml(response);
        log.info("返回微信消息: {}", responseXml);
        return responseXml;
    }
}
