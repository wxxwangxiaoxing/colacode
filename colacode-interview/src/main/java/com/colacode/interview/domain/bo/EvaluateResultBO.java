package com.colacode.interview.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluateResultBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal ruleScore = BigDecimal.ZERO;
    private BigDecimal aiScore;
    private BigDecimal finalScore = BigDecimal.ZERO;
    private List<String> hitPoints = new ArrayList<>();
    private List<String> missPoints = new ArrayList<>();
    private List<String> wrongPoints = new ArrayList<>();
    private String comment;
}