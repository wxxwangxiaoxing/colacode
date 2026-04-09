package com.colacode.circle.common;

import java.util.HashMap;
import java.util.Map;

public class DFAFilter {

    private final Map<Object, Object> sensitiveWordMap = new HashMap<>();

    private static final String END_FLAG = "isEnd";

    public void addWord(String word) {
        Map<Object, Object> currentMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object subMap = currentMap.get(c);
            if (subMap == null) {
                Map<Object, Object> newMap = new HashMap<>();
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Object, Object>) subMap;
            }
        }
        currentMap.put(END_FLAG, true);
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            Map<Object, Object> currentMap = sensitiveWordMap;
            int matchLength = 0;
            int j = i;
            boolean found = false;

            while (j < text.length()) {
                char c = text.charAt(j);
                Object subMap = currentMap.get(c);
                if (subMap == null) break;

                currentMap = (Map<Object, Object>) subMap;
                matchLength++;
                j++;

                if (currentMap.containsKey(END_FLAG)) {
                    found = true;
                }
            }

            if (found) {
                for (int k = 0; k < matchLength; k++) {
                    result.append("*");
                }
                i += matchLength;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    public boolean containsSensitiveWord(String text) {
        return !filter(text).equals(text);
    }
}
