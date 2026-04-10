package com.colacode.auth.application.controller;

import com.colacode.auth.application.dto.AssignRolePermissionsDTO;
import com.colacode.auth.domain.service.RoleDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/rolePermission")
@Tag(name = "角色权限管理", description = "角色与权限的关联管理")
public class RolePermissionController {

    private final RoleDomainService roleDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public RolePermissionController(RoleDomainService roleDomainService,
                                    AdminAuthorizationSupport adminAuthorizationSupport) {
        this.roleDomainService = roleDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    @PostMapping("/assign")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    public Result<Void> assignPermissions(@Valid @RequestBody AssignRolePermissionsDTO dto) {
        adminAuthorizationSupport.assertAdminAccess();
        roleDomainService.assignPermissionsToRole(dto.getRoleId(), dto.getPermissionIds());
        return Result.success();
    }
}
