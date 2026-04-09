package com.colacode.auth.application.controller;

import com.colacode.auth.application.converter.PermissionDTOConverter;
import com.colacode.auth.application.dto.PermissionDTO;
import com.colacode.auth.domain.bo.PermissionBO;
import com.colacode.auth.domain.service.PermissionDomainService;
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

@RestController
@RequestMapping("/auth/permission")
public class PermissionController {

    private final PermissionDomainService permissionDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public PermissionController(PermissionDomainService permissionDomainService,
                                AdminAuthorizationSupport adminAuthorizationSupport) {
        this.permissionDomainService = permissionDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    @PostMapping("/add")
    public Result<Void> addPermission(@RequestBody PermissionDTO permissionDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        PermissionBO permissionBO = PermissionDTOConverter.INSTANCE.convertToBO(permissionDTO);
        permissionDomainService.addPermission(permissionBO);
        return Result.success();
    }

    @PostMapping("/update")
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
    public Result<Void> deletePermission(@RequestBody PermissionDTO permissionDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (permissionDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "权限ID不能为空");
        }
        permissionDomainService.deletePermission(permissionDTO.getId());
        return Result.success();
    }

    @GetMapping("/tree")
    public Result<List<PermissionDTO>> getPermissionTree() {
        adminAuthorizationSupport.assertAdminAccess();
        List<PermissionBO> permissionBOList = permissionDomainService.getPermissionTree();
        List<PermissionDTO> permissionDTOList = PermissionDTOConverter.INSTANCE.convertToDTOList(permissionBOList);
        return Result.success(permissionDTOList);
    }

    @GetMapping("/user/{userId}")
    public Result<List<PermissionDTO>> getPermissionsByUserId(@PathVariable Long userId) {
        if (!adminAuthorizationSupport.canAccessUser(userId)) {
            throw new BusinessException(ResultCodeEnum.USER_PERMISSION_VIEW_FORBIDDEN);
        }
        List<PermissionBO> permissionBOList = permissionDomainService.getPermissionsByUserId(userId);
        List<PermissionDTO> permissionDTOList = PermissionDTOConverter.INSTANCE.convertToDTOList(permissionBOList);
        return Result.success(permissionDTOList);
    }
}
