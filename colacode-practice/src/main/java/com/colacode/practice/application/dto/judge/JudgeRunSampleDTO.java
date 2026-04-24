package com.colacode.practice.application.dto.judge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JudgeRunSampleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "题目ID不能为空")
    private Long subjectId;

    @NotBlank(message = "语言不能为空")
    private String language;

    @NotBlank(message = "代码不能为空")
    private String code;
}
