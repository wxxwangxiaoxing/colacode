package com.colacode.ai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiJudgeAnalysisReqDTO {

    private String subjectName;

    private String language;

    private String code;

    @NotBlank(message = "判题状态不能为空")
    private String status;

    private String judgeMessage;

    private String failedCaseSummary;

    private String stdoutPreview;

    private String stderrPreview;

    private String inputExample;

    private String outputExample;
}
