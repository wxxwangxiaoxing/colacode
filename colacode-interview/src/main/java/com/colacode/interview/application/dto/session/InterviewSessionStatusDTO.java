package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InterviewSessionStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String status;
    private Integer currentQuestionNo;
    private Integer totalQuestionCount;
    private Long reportId;
    private BigDecimal totalScore;
    private InterviewSessionQuestionDTO currentQuestion;
}