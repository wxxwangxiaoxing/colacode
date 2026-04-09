package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewQuestionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String labelName;
    private String keyWord;
    private String subjectName;
    private String subjectAnswer;
    private String userAnswer;
    private Double userScore;
}
