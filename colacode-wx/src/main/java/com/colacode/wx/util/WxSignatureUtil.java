package com.colacode.wx.util;

import java.security.MessageDigest;
import java.util.Arrays;

public class WxSignatureUtil {

    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);

        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            String tmpStr = byteToHex(digest);
            return tmpStr != null && tmpStr.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private static String byteToHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
