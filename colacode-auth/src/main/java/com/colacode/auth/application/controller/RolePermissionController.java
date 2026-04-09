package com.colacode.auth.application.controller;

import com.colacode.auth.application.dto.RolePermissionDTO;
import com.colacode.auth.domain.service.RoleDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/rolePermission")
public class RolePermissionController {

    private final RoleDomainService roleDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public RolePermissionController(RoleDomainService roleDomainService,
                                    AdminAuthorizationSupport adminAuthorizationSupport) {
        this.roleDomainService = roleDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    @PostMapping("/assign")
    public Result<Void> assignPermissions(@RequestBody RolePermissionDTO dto) {
        adminAuthorizationSupport.assertAdminAccess();
        if (dto.getRoleId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "角色ID不能为空");
        }
        roleDomainService.assignPermissionsToRole(dto.getRoleId(), dto.getPermissionIds());
        return Result.success();
    }
}
