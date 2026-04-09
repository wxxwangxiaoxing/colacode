package com.colacode.subject.application.mq;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectEsSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private Integer operation;

    private Integer retryCount;

    private String traceId;

    private Long taskId;
}