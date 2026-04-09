package com.colacode.subject.infra.es;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectEsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String subjectName;

    private String subjectParse;

    private String subjectComment;

    private Integer subjectDiff;

    private Integer subjectType;

    private String categoryName;

    private String labelName;
}
