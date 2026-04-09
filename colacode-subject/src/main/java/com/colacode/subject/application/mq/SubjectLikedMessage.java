package com.colacode.subject.application.mq;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectLikedMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long subjectId;

    private Long likedUserId;

    private Integer likedStatus;
}
