package com.colacode.practice.application.feign.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiJudgeAnalysisRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String feedback;
}
