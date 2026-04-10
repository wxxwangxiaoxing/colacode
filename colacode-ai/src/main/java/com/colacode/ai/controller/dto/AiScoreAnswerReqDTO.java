package com.colacode.ai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 面试答案评分请求DTO
 *
 * @author wxx
 */
@Data
public class AiScoreAnswerReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 面试题
     */
    @NotBlank(message = "question不能为空")
    private String question;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * API密钥（可选）
     */
    private String apiKey;
}
