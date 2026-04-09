package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class InterviewSessionReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long sessionId;
    private String sessionStatus;
    private BigDecimal totalScore;
    private BigDecimal baseScore;
    private BigDecimal logicScore;
    private BigDecimal expressionScore;
    private BigDecimal engineeringScore;
    private String summary;
    private String suggestion;
    private List<String> weaknessTags = new ArrayList<>();
    private List<String> advantageTags = new ArrayList<>();
    private List<InterviewReportQuestionDTO> questions = new ArrayList<>();
}