package com.colacode.practice.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PracticeSubmitBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long setId;

    private Long userId;

    private List<AnswerItemBO> answers;

    @Data
    public static class AnswerItemBO implements Serializable {
        private Long subjectId;
        private String userAnswer;
        private Integer timeUse;
    }
}
