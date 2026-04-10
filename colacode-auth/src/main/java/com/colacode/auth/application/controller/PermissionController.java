package com.colacode.auth.application.controller;

import com.colacode.auth.application.converter.PermissionDTOConverter;
import com.colacode.auth.application.dto.PermissionDTO;
import com.colacode.auth.domain.bo.PermissionBO;
import com.colacode.auth.domain.service.PermissionDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理控制器
 * 提供权限相关的RESTful接口
 *
 * @author wxx
 */
@RestController
@RequestMapping("/auth/permission")
@Tag(name = "权限管理", description = "权限信息的增删改查")
public class PermissionController {

    private final PermissionDomainService permissionDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public PermissionController(PermissionDomainService permissionDomainService,
                                AdminAuthorizationSupport adminAuthorizationSupport) {
        this.permissionDomainService = permissionDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    /**
     * 新增权限接口
     *
     * @param permissionDTO 权限信息
     * @return 添加成功
     */
    @PostMapping("/add")
    @Operation(summary = "新增权限", description = "添加新权限")
    public Result<Void> addPermission(@RequestBody PermissionDTO permissionDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        PermissionBO permissionBO = PermissionDTOConverter.INSTANCE.convertToBO(permissionDTO);
        permissionDomainService.addPermission(permissionBO);
        return Result.success();
    }

    /**
     * 更新权限接口
     *
     * @param permissionDTO 权限信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<Void> updatePermission(@RequestBody PermissionDTO permissionDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (permissionDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "权限ID不能为空");
        }
        PermissionBO permissionBO = PermissionDTOConverter.INSTANCE.convertToBO(permissionDTO);
        permissionDomainService.updatePermission(permissionBO);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除权限", description = "删除权限")
    public Result<Void> deletePermission(@RequestBody PermissionDTO permissionDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (permissionDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "权限ID不能为空");
        }
        permissionDomainService.deletePermission(permissionDTO.getId());
        return Result.success();
    }

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取所有权限的树形结构")
    public Result<List<PermissionDTO>> getPermissionTree() {
        adminAuthorizationSupport.assertAdminAccess();
        List<PermissionBO> permissionBOList = permissionDomainService.getPermissionTree();
        List<PermissionDTO> permissionDTOList = PermissionDTOConverter.INSTANCE.convertToDTOList(permissionBOList);
        return Result.success(permissionDTOList);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    public Result<List<PermissionDTO>> getPermissionsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (!adminAuthorizationSupport.canAccessUser(userId)) {
            throw new BusinessException(ResultCodeEnum.USER_PERMISSION_VIEW_FORBIDDEN);
        }
        List<PermissionBO> permissionBOList = permissionDomainService.getPermissionsByUserId(userId);
        List<PermissionDTO> permissionDTOList = PermissionDTOConverter.INSTANCE.convertToDTOList(permissionBOList);
        return Result.success(permissionDTOList);
    }
}
