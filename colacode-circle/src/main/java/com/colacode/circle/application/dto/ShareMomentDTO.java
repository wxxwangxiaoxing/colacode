package com.colacode.circle.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShareMomentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "圈子ID不能为空")
    private Long circleId;

    @NotBlank(message = "动态内容不能为空")
    @Size(max = 2000, message = "动态内容长度不能超过2000个字符")
    private String content;

    @Size(max = 2000, message = "图片地址长度不能超过2000个字符")
    private String images;

    private Long userId;

    private Integer likedCount;
}
