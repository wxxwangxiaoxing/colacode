package com.colacode.subject.infra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("subject_info")
public class SubjectInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectName;

    @TableField("subject_difficulty")
    private Integer subjectDiff;

    private Integer subjectType;

    private String subjectParse;

    @TableField(exist = false)
    private String subjectComment;

    @TableField("browse_count")
    private Long browseCount;

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
