package com.colacode.auth.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String roleName;

    private String roleKey;

    private List<Long> permissionIds;
}
