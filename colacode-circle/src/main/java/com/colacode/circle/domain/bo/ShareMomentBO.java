package com.colacode.circle.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareMomentBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long circleId;

    private String content;

    private String images;

    private Long userId;

    private Integer likedCount;
}
