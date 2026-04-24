package com.colacode.subject.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubjectInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String subjectName;

    private Integer subjectDiff;

    private Integer subjectType;

    private String subjectParse;

    private String subjectComment;

    private Long browseCount;

    private List<Long> categoryIds;

    private List<Long> labelIds;

    private List<SubjectOptionDTO> optionList;

    private String correctAnswer;

    private String briefContent;

    private SubjectCodeConfigDTO codeConfig;

    private List<SubjectCodeCaseDTO> testCases;
}
