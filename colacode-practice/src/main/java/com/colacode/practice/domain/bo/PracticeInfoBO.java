package com.colacode.practice.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PracticeInfoBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long setId;

    private Long userId;

    private Integer totalScore;

    private Integer correctCount;

    private Integer wrongCount;

    private String submitTime;

    private String setName;

    private Integer timeUse;

    private PracticeDetailBO detail;
}
