package com.colacode.practice.application.dto.judge;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JudgeSubmitRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long submissionId;

    private String status;
}
