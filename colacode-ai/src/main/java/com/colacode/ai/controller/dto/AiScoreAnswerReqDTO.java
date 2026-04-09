package com.colacode.ai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class AiScoreAnswerReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "question不能为空")
    private String question;

    private String userAnswer;

    private String apiKey;
}
