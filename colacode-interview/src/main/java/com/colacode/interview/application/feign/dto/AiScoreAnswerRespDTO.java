package com.colacode.interview.application.feign.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiScoreAnswerRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double score;
}
