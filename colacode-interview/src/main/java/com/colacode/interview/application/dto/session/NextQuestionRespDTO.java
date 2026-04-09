package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class NextQuestionRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String status;
    private Boolean finished;
    private InterviewSessionQuestionDTO nextQuestion;
    private Long reportId;
}