package com.colacode.circle.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ShareMessageBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer messageType;

    private Long momentId;

    private Integer status;

    private Date createdTime;
}
