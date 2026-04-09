package com.colacode.circle.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareCircleBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String content;

    private Long userId;

    private Integer likedCount;

    private Integer commentCount;
}
