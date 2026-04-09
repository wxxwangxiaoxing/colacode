package com.colacode.practice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PracticeSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "套题ID不能为空")
    private Long setId;

    private Long userId;

    @Valid
    @NotEmpty(message = "作答列表不能为空")
    private List<AnswerItemDTO> answers;

    @Data
    public static class AnswerItemDTO implements Serializable {
        @NotNull(message = "题目ID不能为空")
        private Long subjectId;
        private String userAnswer;
        private Integer timeUse;
    }
}
