package com.colacode.circle.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShareCommentReplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "目标ID不能为空")
    private Long momentId;

    @NotNull(message = "圈子ID不能为空")
    private Long circleId;

    private Long userId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;

    private Long replyUserId;

    @NotNull(message = "评论类型不能为空")
    private Integer type;
}
