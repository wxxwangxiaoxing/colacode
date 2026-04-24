package com.colacode.practice.application.feign.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SubjectCodeConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String judgeMode;

    private Integer timeLimitMs;

    private Integer memoryLimitKb;

    private List<String> supportedLanguages;

    private Map<String, String> templateCode;

    private String inputExample;

    private String outputExample;
}
