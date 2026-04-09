package com.colacode.circle.infra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sensitive_words")
public class SensitiveWords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String words;

    private Integer type;

    @TableLogic
    private Integer isDeleted;
}
