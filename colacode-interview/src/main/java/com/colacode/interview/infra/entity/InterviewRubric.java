package com.colacode.interview.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("interview_rubric")
public class InterviewRubric implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String postType;
    private String questionType;
    private String category;
    private BigDecimal totalScore;
    private String scoringItemsJson;
    private Integer version;
    private Integer enabled;
    private Date createdTime;
    private Date updatedTime;
}