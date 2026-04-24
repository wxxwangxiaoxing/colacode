package com.colacode.practice.application.dto.judge;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class JudgeRunSampleResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String overallStatus;

    private String message;

    private List<SampleCaseResultDTO> results;

    @Data
    public static class SampleCaseResultDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Integer caseNo;

        private String status;

        private String stdin;

        private String expectedStdout;

        private String actualStdout;

        private String stderr;

        private Integer executeTimeMs;

        private Integer memoryUsedKb;

        private String message;
    }
}
