package com.colacode.subject.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("subject_code_case")
public class SubjectCodeCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private Integer caseNo;

    @TableField("stdin_text")
    private String stdinText;

    @TableField("expected_stdout")
    private String expectedStdout;

    @TableField("is_sample")
    private Integer sampleCase;

    private Integer score;

    private Date createdTime;

    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
