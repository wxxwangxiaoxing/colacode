package com.colacode.circle.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class MessageReadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "消息ID不能为空")
    private Long messageId;
}
