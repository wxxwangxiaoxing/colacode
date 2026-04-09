package com.colacode.circle.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class SensitiveWordCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "敏感词不能为空")
    @Size(max = 255, message = "敏感词长度不能超过255个字符")
    private String words;

    @NotNull(message = "敏感词类型不能为空")
    private Integer type;
}
