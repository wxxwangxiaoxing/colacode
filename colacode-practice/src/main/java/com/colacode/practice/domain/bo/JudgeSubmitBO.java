package com.colacode.practice.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JudgeSubmitBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private String language;

    private String code;
}
