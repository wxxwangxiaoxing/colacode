package com.colacode.auth.application.controller;

import com.colacode.auth.application.dto.CreateRoleDTO;
import com.colacode.auth.application.dto.DeleteRoleDTO;
import com.colacode.auth.application.dto.RoleDTO;
import com.colacode.auth.application.dto.UnassignRoleDTO;
import com.colacode.auth.application.dto.UpdateRoleDTO;
import com.colacode.auth.application.dto.UserRoleDTO;
import com.colacode.auth.domain.bo.RoleBO;
import com.colacode.auth.domain.service.RoleDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 * 提供角色相关的RESTful接口
 *
 * @author wxx
 */
@RestController
@RequestMapping("/auth/role")
@Tag(name = "角色管理", description = "角色信息的增删改查")
public class RoleController {

    private final RoleDomainService roleDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public RoleController(RoleDomainService roleDomainService,
                          AdminAuthorizationSupport adminAuthorizationSupport) {
        this.roleDomainService = roleDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    /**
     * 获取用户角色列表接口
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/list/{userId}")
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
    public Result<List<RoleDTO>> getRolesByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
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

    /**
     * 获取所有角色列表接口
     *
     * @return 所有角色列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有角色", description = "获取所有角色列表")
    public Result<List<RoleDTO>> listAllRoles() {
        adminAuthorizationSupport.assertAdminAccess();
        List<RoleBO> roleBOList = roleDomainService.listAllRoles();
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

    /**
     * 新增角色接口
     *
     * @param roleDTO 角色信息
     * @return 添加成功
     */
    @PostMapping("/add")
    @Operation(summary = "新增角色", description = "创建新角色")
    public Result<Void> addRole(@Valid @RequestBody CreateRoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        RoleBO roleBO = new RoleBO();
        roleBO.setRoleName(roleDTO.getRoleName());
        roleBO.setRoleKey(roleDTO.getRoleKey());
        roleBO.setPermissionIds(roleDTO.getPermissionIds());
        roleDomainService.addRole(roleBO);
        return Result.success();
    }

    /**
     * 分配角色接口
     *
     * @param userRoleDTO 用户角色信息
     * @return 分配成功
     */
    @PostMapping("/assign")
    @Operation(summary = "分配角色", description = "为用户分配角色")
    public Result<Void> assignRoleToUser(@Valid @RequestBody UserRoleDTO userRoleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        roleDomainService.assignRoleToUser(userRoleDTO.getUserId(), userRoleDTO.getRoleId());
        return Result.success();
    }

    /**
     * 取消分配角色接口
     *
     * @param unassignRoleDTO 用户角色信息
     * @return 取消成功
     */
    @PostMapping("/unassign")
    @Operation(summary = "取消分配角色", description = "取消用户的角色")
    public Result<Void> unassignRoleFromUser(@Valid @RequestBody UnassignRoleDTO unassignRoleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        roleDomainService.unassignRoleFromUser(unassignRoleDTO.getUserId(), unassignRoleDTO.getRoleId());
        return Result.success();
    }

    /**
     * 更新角色信息接口
     *
     * @param roleDTO 角色信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<Void> updateRole(@Valid @RequestBody UpdateRoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        RoleBO roleBO = new RoleBO();
        roleBO.setId(roleDTO.getId());
        roleBO.setRoleName(roleDTO.getRoleName());
        roleBO.setRoleKey(roleDTO.getRoleKey());
        roleBO.setPermissionIds(roleDTO.getPermissionIds());
        roleDomainService.updateRole(roleBO);
        return Result.success();
    }

    /**
     * 删除角色接口
     *
     * @param roleDTO 角色信息
     * @return 删除成功
     */
    @PostMapping("/delete")
    @Operation(summary = "删除角色", description = "删除角色")
    public Result<Void> deleteRole(@Valid @RequestBody DeleteRoleDTO roleDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        roleDomainService.deleteRole(roleDTO.getId());
        return Result.success();
    }
}
