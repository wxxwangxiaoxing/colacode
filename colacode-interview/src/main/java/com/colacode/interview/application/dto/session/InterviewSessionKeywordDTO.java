package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewSessionKeywordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyWord;
    private Long categoryId;
    private Long labelId;
}