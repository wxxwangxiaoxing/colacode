package com.colacode.interview.infra.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("interview_question_record")
public class InterviewQuestionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long questionId;
    private String questionSource;
    private String questionType;
    private String category;
    private Integer difficulty;
    private String stem;
    private String standardAnswer;
    private Long rubricId;
    private Integer roundNo;
    private Long parentRecordId;
    private Integer isFollowUp;
    private String keyWords;
    private String userAnswer;
    private BigDecimal ruleScore;
    private BigDecimal aiScore;
    private BigDecimal finalScore;
    private String hitKeywords;
    private String missKeywords;
    private String wrongPoints;
    private String evaluationComment;
    private Date askTime;
    private Date answerTime;
    private Integer costSeconds;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableLogic
    private Integer isDeleted;
}