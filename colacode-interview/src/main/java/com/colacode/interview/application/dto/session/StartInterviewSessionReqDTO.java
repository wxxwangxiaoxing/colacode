package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StartInterviewSessionReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String engineType;
    private String interviewType;
    private String postType;
    private Integer difficultyLevel;
    private Integer questionCount;
    private String sourceMode;
    private List<InterviewSessionKeywordDTO> keywords;
}