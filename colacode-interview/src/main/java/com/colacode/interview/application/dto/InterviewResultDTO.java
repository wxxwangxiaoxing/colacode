package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InterviewResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double avgScore;
    private List<String> tips;
    private String avgTips;
}
