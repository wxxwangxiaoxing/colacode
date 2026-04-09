package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Double avgScore;
    private String keyWords;
    private String tip;
    private Object createdTime;
}
