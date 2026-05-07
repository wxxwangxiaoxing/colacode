package com.colacode.ai.service.dto;

import lombok.Data;

@Data
public class JudgeAnalysisContext {

    private String subjectName;

    private String language;

    private String code;

    private String status;

    private String judgeMessage;

    private String failedCaseSummary;

    private String stdoutPreview;

    private String stderrPreview;

    private String inputExample;

    private String outputExample;
}
