package com.colacode.circle.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShareCircleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "圈子标题不能为空")
    @Size(max = 64, message = "圈子标题长度不能超过64个字符")
    private String title;

    @NotBlank(message = "圈子内容不能为空")
    @Size(max = 2000, message = "圈子内容长度不能超过2000个字符")
    private String content;

    private Long userId;

    private Integer likedCount;

    private Integer commentCount;
}
