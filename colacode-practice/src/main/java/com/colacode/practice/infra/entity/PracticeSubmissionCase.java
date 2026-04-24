package com.colacode.practice.infra.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("practice_submission_case")
public class PracticeSubmissionCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("case_no")
    private Integer caseNo;

    @TableField("is_sample")
    private Integer sampleCase;

    private String status;

    @TableField("stdin_text")
    private String stdinText;

    @TableField("expected_stdout")
    private String expectedStdout;

    @TableField("actual_stdout")
    private String actualStdout;

    @TableField("stderr_text")
    private String stderrText;

    @TableField("execute_time_ms")
    private Integer executeTimeMs;

    @TableField("memory_used_kb")
    private Integer memoryUsedKb;

    @TableField("judge_token")
    private String judgeToken;

    @TableField("judge_message")
    private String judgeMessage;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private Date createdTime;
}
