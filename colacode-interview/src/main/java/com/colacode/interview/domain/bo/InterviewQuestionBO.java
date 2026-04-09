package com.colacode.interview.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewQuestionBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String labelName;

    private String keyWord;

    private String subjectName;

    private String subjectAnswer;

    private String userAnswer;

    private Double userScore;
}
