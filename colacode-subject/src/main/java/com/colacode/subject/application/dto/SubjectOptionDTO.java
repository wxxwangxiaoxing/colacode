package com.colacode.subject.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubjectOptionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String optionType;

    private String optionContent;

    private Integer isCorrect;
}
