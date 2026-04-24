package com.colacode.practice.application.dto.judge;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class JudgeSubmissionDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long subjectId;

    private String language;

    private String status;

    private Integer passCaseCount;

    private Integer totalCaseCount;

    private Integer executeTimeMs;

    private Integer memoryUsedKb;

    private String judgeMessage;

    private String stdoutPreview;

    private String stderrPreview;

    private String aiStatus;

    private String aiFeedback;

    private Date createdTime;

    private List<JudgeSubmissionCaseDTO> cases;

    private String failedCaseSummary;
}
