package com.colacode.practice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    private String baseUrl;

    private Integer pollIntervalMs = 500;

    private Integer maxPollCount = 20;

    private Integer maxCodeLength = 65536;

    private Integer maxSubmitPerMinute = 20;

    private Integer submitCooldownSeconds = 5;

    private Map<String, Integer> languages = new LinkedHashMap<>();

    private AiProperties ai = new AiProperties();

    @Data
    public static class AiProperties {

        private boolean enabled;

        private boolean includeAccepted;

        private Integer maxCodeContextLength = 6000;

        private Integer maxFeedbackLength = 4000;
    }
}
