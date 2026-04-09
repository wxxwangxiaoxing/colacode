package com.colacode.practice.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PracticeDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long practiceId;
    private Long subjectId;
    private String userAnswer;
    private String correctAnswer;
    private Integer isCorrect;
    private Integer timeUse;
}
