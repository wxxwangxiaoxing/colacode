package com.colacode.subject.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectLabelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String labelName;

    private Long categoryId;

    private Integer sortNum;
}
