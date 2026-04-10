package com.colacode.subject.application.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubjectEsSyncMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Long subjectId;

    private Integer operation;

    /**
     * 仅用于观测消息重投情况，不作为业务补偿次数的权威来源。
     */
    private Integer retryCount;

    private String traceId;

    private String payloadJson;
}
