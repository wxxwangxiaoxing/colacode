package com.colacode.interview.application.feign.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiScoreAnswerReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String question;

    private String userAnswer;

    private String apiKey;
}
