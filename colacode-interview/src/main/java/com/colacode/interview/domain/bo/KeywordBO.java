package com.colacode.interview.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class KeywordBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyWord;

    private Long categoryId;

    private Long labelId;
}
