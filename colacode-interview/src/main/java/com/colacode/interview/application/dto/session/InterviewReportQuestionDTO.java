package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class InterviewReportQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;
    private String stem;
    private String userAnswer;
    private BigDecimal finalScore;
    private List<String> hitPoints = new ArrayList<>();
    private List<String> missPoints = new ArrayList<>();
    private List<String> wrongPoints = new ArrayList<>();
    private String comment;
}