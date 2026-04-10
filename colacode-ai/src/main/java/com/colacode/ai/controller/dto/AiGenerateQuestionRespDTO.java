package com.colacode.ai.controller.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成面试题响应DTO
 *
 * @author wxx
 */
@Data
public class AiGenerateQuestionRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 生成的面试题内容
     */
    private String question;
}
