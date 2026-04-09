package com.colacode.subject.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectJudgeBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String isCorrect;
}
