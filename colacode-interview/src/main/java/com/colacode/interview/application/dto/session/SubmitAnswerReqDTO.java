package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubmitAnswerReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private Long recordId;
    private String answer;
}