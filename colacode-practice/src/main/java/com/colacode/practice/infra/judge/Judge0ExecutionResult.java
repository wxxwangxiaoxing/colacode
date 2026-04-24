package com.colacode.practice.infra.judge;

import lombok.Data;

@Data
public class Judge0ExecutionResult {

    private String token;

    private Integer statusId;

    private String statusDescription;

    private String stdout;

    private String stderr;

    private Integer executeTimeMs;

    private Integer memoryUsedKb;
}
