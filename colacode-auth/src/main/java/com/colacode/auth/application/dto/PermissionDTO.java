package com.colacode.auth.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long parentId;

    private Integer type;

    private String menuUrl;

    private Integer status;

    private Integer show;

    private String icon;

    private String permissionKey;

    private List<PermissionDTO> children;
}
