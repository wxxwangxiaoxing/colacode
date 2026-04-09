package com.colacode.interview.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("interview_report")
public class InterviewReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long userId;
    private BigDecimal totalScore;
    private BigDecimal baseScore;
    private BigDecimal logicScore;
    private BigDecimal expressionScore;
    private BigDecimal engineeringScore;
    private String summary;
    private String weaknessTagsJson;
    private String advantageTagsJson;
    private String suggestion;
    private String recommendedPracticeJson;
    private Date createdTime;
    private Date updatedTime;
}