package com.colacode.auth.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeUserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotNull(message = "用户状态不能为空")
    private Integer status;
}
