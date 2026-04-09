package com.colacode.practice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class PracticeSetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "套题名称不能为空")
    @Size(max = 64, message = "套题名称长度不能超过64个字符")
    private String setName;

    @Size(max = 500, message = "套题描述长度不能超过500个字符")
    private String description;

    private Integer status;
}
