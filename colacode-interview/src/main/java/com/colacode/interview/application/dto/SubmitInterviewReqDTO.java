package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SubmitInterviewReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String engineType;

    private String interviewUrl;

    private Long userId;

    private List<QuestionAnswerDTO> questions;

    @Data
    public static class QuestionAnswerDTO implements Serializable {
        private String keyWord;
        private String subjectName;
        private String subjectAnswer;
        private String userAnswer;
        private Double userScore;
    }
}
