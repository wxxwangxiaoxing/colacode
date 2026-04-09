package com.colacode.wx.handler;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;

public interface WxMessageHandler {

    String getMsgType();

    String getEvent();

    WxTextMessage handle(WxMessage wxMessage);
}
