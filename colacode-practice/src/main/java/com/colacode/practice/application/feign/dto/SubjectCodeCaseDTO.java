package com.colacode.practice.application.feign.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubjectCodeCaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer caseNo;

    private String stdinText;

    private String expectedStdout;

    private Integer sampleCase;

    private Integer score;
}
