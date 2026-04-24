package com.colacode.practice.infra.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.JdbcType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("practice_submission")
public class PracticeSubmission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("subject_id")
    private Long subjectId;

    private String language;

    @TableField("language_id")
    private Integer languageId;

    private byte[] code;

    private String status;

    @TableField("pass_case_count")
    private Integer passCaseCount;

    @TableField("total_case_count")
    private Integer totalCaseCount;

    @TableField("execute_time_ms")
    private Integer executeTimeMs;

    @TableField("memory_used_kb")
    private Integer memoryUsedKb;

    @TableField("judge_message")
    private String judgeMessage;

    @TableField("stdout_preview")
    private String stdoutPreview;

    @TableField("stderr_preview")
    private String stderrPreview;

    @TableField("ai_status")
    private String aiStatus;

    @TableField("ai_feedback")
    private String aiFeedback;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
