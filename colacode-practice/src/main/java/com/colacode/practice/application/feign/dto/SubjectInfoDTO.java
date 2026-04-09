package com.colacode.practice.application.feign.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String subjectName;

    private Integer subjectDiff;

    private Integer subjectType;

    private String subjectParse;

    private String subjectComment;
}
