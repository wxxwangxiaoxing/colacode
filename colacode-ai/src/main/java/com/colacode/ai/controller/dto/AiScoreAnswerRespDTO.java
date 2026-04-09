package com.colacode.ai.controller.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiScoreAnswerRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double score;
}
