package com.colacode.wx.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("xml")
public class WxTextMessage {

    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private Long createTime;

    @XStreamAlias("MsgType")
    private String msgType = "text";

    @XStreamAlias("Content")
    private String content;

    public WxTextMessage() {
    }

    public WxTextMessage(String fromUserName, String toUserName, String content) {
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.content = content;
        this.createTime = System.currentTimeMillis() / 1000;
    }
}
