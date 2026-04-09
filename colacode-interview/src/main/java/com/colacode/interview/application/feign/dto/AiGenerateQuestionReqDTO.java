package com.colacode.interview.application.feign.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiGenerateQuestionReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;

    private String apiKey;
}
