package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AnalyseReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String engineType;

    private List<String> labels;
}
