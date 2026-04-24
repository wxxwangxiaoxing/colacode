package com.colacode.practice.domain.service;

import org.springframework.stereotype.Component;

@Component
public class JudgeOutputComparator {

    public boolean matches(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String normalize(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("\r\n", "\n").trim();
    }
}
