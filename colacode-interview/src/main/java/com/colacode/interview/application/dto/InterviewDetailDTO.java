package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double score;
    private String keyWords;
    private String question;
    private String answer;
    private String userAnswer;
}
