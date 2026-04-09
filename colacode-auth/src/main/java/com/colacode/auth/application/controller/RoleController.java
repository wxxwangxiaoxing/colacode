package com.colacode.auth.application.controller;

import com.colacode.auth.application.dto.RoleDTO;
import com.colacode.auth.application.dto.UserRoleDTO;
import com.colacode.auth.domain.bo.RoleBO;
import com.colacode.auth.domain.service.RoleDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth/role")
public class RoleController {

    private final RoleDomainService roleDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public RoleController(RoleDomainService roleDomainService,
                          AdminAuthorizationSupport adminAuthorizationSupport) {
        this.roleDomainService = roleDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    @GetMapping("/list/{userId}")
    public Result<List<RoleDTO>> getRolesByUserId(@PathVariable Long userId) {
        if (!adminAuthorizationSupport.canAccessUser(userId)) {
            throw new BusinessException(ResultCodeEnum.USER_ROLE_VIEW_FORBIDDEN);
        }
        List<RoleBO> roleBOList = roleDomainService.getRolesByUserId(userId);
        List<RoleDTO> roleDTOList = roleBOList.stream().map(bo -> {
            RoleDTO dto = new RoleDTO();
            dto.setId(bo.getId());
            dto.setRoleName(bo.getRoleName());
            dto.setRoleKey(bo.getRoleKey());
            dto.setPermissionIds(bo.getPermissionIds());
            return dto;
        }).collect(Collectors.toList());
        return Result.success(roleDTOList);
    }

    @PostMapping("/add")
    public Result<Void> addRole(@RequestBody RoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        RoleBO roleBO = new RoleBO();
        roleBO.setRoleName(roleDTO.getRoleName());
        roleBO.setRoleKey(roleDTO.getRoleKey());
        roleBO.setPermissionIds(roleDTO.getPermissionIds());
        roleDomainService.addRole(roleBO);
        return Result.success();
    }

    @PostMapping("/assign")
    public Result<Void> assignRoleToUser(@RequestBody UserRoleDTO userRoleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        roleDomainService.assignRoleToUser(userRoleDTO.getUserId(), userRoleDTO.getRoleId());
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateRole(@RequestBody RoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (roleDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "角色ID不能为空");
        }
        RoleBO roleBO = new RoleBO();
        roleBO.setId(roleDTO.getId());
        roleBO.setRoleName(roleDTO.getRoleName());
        roleBO.setRoleKey(roleDTO.getRoleKey());
        roleBO.setPermissionIds(roleDTO.getPermissionIds());
        roleDomainService.updateRole(roleBO);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestBody RoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (roleDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "角色ID不能为空");
        }
        roleDomainService.deleteRole(roleDTO.getId());
        return Result.success();
    }
}
