package com.colacode.circle.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SensitiveWordBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String words;

    private Integer type;
}
