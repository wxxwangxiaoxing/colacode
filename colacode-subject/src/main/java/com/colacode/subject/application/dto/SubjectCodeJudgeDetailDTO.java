package com.colacode.subject.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubjectCodeJudgeDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private String subjectName;

    private Integer subjectType;

    private SubjectCodeConfigDTO codeConfig;

    private List<SubjectCodeCaseDTO> testCases;
}
