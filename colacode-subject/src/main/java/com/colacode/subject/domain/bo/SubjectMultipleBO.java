package com.colacode.subject.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectMultipleBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String optionType;

    private String optionContent;

    private Integer isCorrect;
}
