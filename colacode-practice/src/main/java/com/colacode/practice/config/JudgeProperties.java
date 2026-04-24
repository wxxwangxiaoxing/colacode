package com.colacode.practice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    private String baseUrl;

    private Integer pollIntervalMs = 500;

    private Integer maxPollCount = 20;

    private Integer maxCodeLength = 65536;

    private Integer maxSubmitPerMinute = 20;

    private Integer submitCooldownSeconds = 5;

    private Map<String, Integer> languages = new LinkedHashMap<>();
}
