package com.colacode.circle.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareCommentReplyBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long momentId;

    private Long circleId;

    private Long userId;

    private String content;

    private Long replyUserId;

    private Integer type;
}
