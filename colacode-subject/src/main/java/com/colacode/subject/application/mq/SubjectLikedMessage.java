package com.colacode.subject.application.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubjectLikedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private Long likedUserId;

    private Integer likedStatus;

    private String traceId;
}
