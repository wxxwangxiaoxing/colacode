package com.colacode.auth.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PermissionBO implements Serializable {

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

    private List<PermissionBO> children;
}
