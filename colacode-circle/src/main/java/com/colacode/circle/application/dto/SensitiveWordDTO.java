package com.colacode.circle.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SensitiveWordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String words;

    private Integer type;
}
