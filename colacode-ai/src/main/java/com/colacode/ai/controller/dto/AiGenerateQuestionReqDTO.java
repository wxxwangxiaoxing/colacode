package com.colacode.ai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiGenerateQuestionReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "keyword不能为空")
    private String keyword;

    private String apiKey;
}
