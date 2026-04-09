package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubmitAnswerRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private Long recordId;
    private String status;
    private InterviewSessionQuestionDTO questionResult;
}