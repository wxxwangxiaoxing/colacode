package com.colacode.circle.infra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("share_comment_reply")
public class ShareCommentReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long momentId;

    @TableField(exist = false)
    private Long circleId;

    @TableField(exist = false)
    private Long userId;

    private String content;

    @TableField("reply_user_id")
    private Long replyUserId;

    @TableField("reply_type")
    private Integer type;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
