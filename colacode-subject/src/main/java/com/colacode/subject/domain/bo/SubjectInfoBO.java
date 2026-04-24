package com.colacode.subject.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubjectInfoBO implements Serializable {

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

    private List<SubjectRadioBO> radioList;

    private List<SubjectMultipleBO> multipleList;

    private SubjectJudgeBO judgeBO;

    private SubjectBriefBO briefBO;

    private SubjectCodeBO codeConfig;

    private List<SubjectCodeCaseBO> testCases;

    private String createdBy;

    private Integer contributeCount;
}
