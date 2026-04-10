package com.colacode.ai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成面试题请求DTO
 *
 * @author wxx
 */
@Data
public class AiGenerateQuestionReqDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词，用于生成面试题
     */
    @NotBlank(message = "keyword不能为空")
    private String keyword;

    /**
     * API密钥（可选）
     */
    private String apiKey;
}
