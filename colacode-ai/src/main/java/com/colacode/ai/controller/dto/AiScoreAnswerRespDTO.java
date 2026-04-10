package com.colacode.ai.controller.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 面试答案评分响应DTO
 *
 * @author wxx
 */
@Data
public class AiScoreAnswerRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分结果（1.0-5.0）
     */
    private Double score;
}
