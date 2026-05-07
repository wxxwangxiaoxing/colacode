package com.colacode.practice.application.feign.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiJudgeAnalysisReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
