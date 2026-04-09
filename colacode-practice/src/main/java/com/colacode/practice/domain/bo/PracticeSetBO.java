package com.colacode.practice.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PracticeSetBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String setName;

    private String description;

    private Integer status;
}
