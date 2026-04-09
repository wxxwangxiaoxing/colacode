package com.colacode.subject.infra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("es_sync_status")
public class EsSyncStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bizId;

    private String bizType;

    private Integer operation;

    private String payloadJson;

    private Integer status;

    private Integer retryCount;

    private Integer maxRetryCount;

    private Date nextRetryTime;

    private Date lastSyncTime;

    private String errorMsg;

    private String traceId;

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

    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAILED = 2;
    public static final Integer STATUS_PROCESSING = 3;
    public static final Integer STATUS_DEAD = 4;

    public static final Integer OPERATION_ADD_UPDATE = 1;
    public static final Integer OPERATION_DELETE = 2;
}