package com.colacode.auth.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.auth.domain.bo.PermissionBO;
import com.colacode.auth.domain.converter.PermissionBOConverter;
import com.colacode.auth.infra.entity.AuthPermission;
import com.colacode.auth.infra.entity.AuthRolePermission;
import com.colacode.auth.infra.entity.AuthUserRole;
import com.colacode.auth.infra.mapper.AuthPermissionMapper;
import com.colacode.auth.infra.mapper.AuthRolePermissionMapper;
import com.colacode.auth.infra.mapper.AuthUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionDomainService {

    private final AuthPermissionMapper authPermissionMapper;
    private final AuthRolePermissionMapper authRolePermissionMapper;
    private final AuthUserRoleMapper authUserRoleMapper;

    public PermissionDomainService(AuthPermissionMapper authPermissionMapper,
                                   AuthRolePermissionMapper authRolePermissionMapper,
                                   AuthUserRoleMapper authUserRoleMapper) {
        this.authPermissionMapper = authPermissionMapper;
        this.authRolePermissionMapper = authRolePermissionMapper;
        this.authUserRoleMapper = authUserRoleMapper;
    }

    public void addPermission(PermissionBO permissionBO) {
        AuthPermission entity = PermissionBOConverter.INSTANCE.convertToEntity(permissionBO);
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        authPermissionMapper.insert(entity);
    }

    public void updatePermission(PermissionBO permissionBO) {
        AuthPermission entity = PermissionBOConverter.INSTANCE.convertToEntity(permissionBO);
        authPermissionMapper.updateById(entity);
    }

    public void deletePermission(Long id) {
        authPermissionMapper.deleteById(id);
    }

    public List<PermissionBO> getPermissionTree() {
        List<AuthPermission> allPermissions = authPermissionMapper.selectList(null);
        List<PermissionBO> allBOs = PermissionBOConverter.INSTANCE.convertToBOList(allPermissions);
        return buildTree(allBOs, 0L);
    }

    public List<PermissionBO> getPermissionsByUserId(Long userId) {
        LambdaQueryWrapper<AuthUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(AuthUserRole::getUserId, userId);
        List<AuthUserRole> userRoles = authUserRoleMapper.selectList(urWrapper);

        List<Long> roleIds = userRoles.stream().map(AuthUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AuthRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.in(AuthRolePermission::getRoleId, roleIds);
        List<AuthRolePermission> rolePermissions = authRolePermissionMapper.selectList(rpWrapper);

        List<Long> permissionIds = rolePermissions.stream()
                .map(AuthRolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AuthPermission> permissions = authPermissionMapper.selectBatchIds(permissionIds);
        List<PermissionBO> allBOs = PermissionBOConverter.INSTANCE.convertToBOList(permissions);
        return buildTree(allBOs, 0L);
    }

    private List<PermissionBO> buildTree(List<PermissionBO> all, Long parentId) {
        return all.stream()
                .filter(p -> Objects.equals(p.getParentId(), parentId))
                .peek(p -> p.setChildren(buildTree(all, p.getId())))
                .collect(Collectors.toList());
    }
}
