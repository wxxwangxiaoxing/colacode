package com.colacode.wx.util;

import com.colacode.wx.dto.WxMessage;
import com.colacode.wx.dto.WxTextMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class WxXmlUtil {

    private static XStream inXStream;
    private static XStream outXStream;

    static {
        inXStream = new XStream(new DomDriver());
        inXStream.processAnnotations(WxMessage.class);
        inXStream.ignoreUnknownElements();

        outXStream = new XStream(new DomDriver());
        outXStream.processAnnotations(WxTextMessage.class);
        outXStream.alias("xml", WxTextMessage.class);
    }

    public static WxMessage xmlToMessage(String xml) {
        return (WxMessage) inXStream.fromXML(xml);
    }

    public static String messageToXml(WxTextMessage message) {
        return outXStream.toXML(message);
    }
}
