package com.colacode.practice.application.dto.judge;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class JudgeSubmissionSummaryDTO implements Serializable {

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

    private Date createdTime;
}
