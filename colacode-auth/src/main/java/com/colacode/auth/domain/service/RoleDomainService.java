package com.colacode.auth.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.auth.domain.bo.RoleBO;
import com.colacode.auth.infra.entity.AuthRole;
import com.colacode.auth.infra.entity.AuthRolePermission;
import com.colacode.auth.infra.entity.AuthUserRole;
import com.colacode.auth.infra.mapper.AuthRoleMapper;
import com.colacode.auth.infra.mapper.AuthRolePermissionMapper;
import com.colacode.auth.infra.mapper.AuthUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleDomainService {

    private final AuthRoleMapper authRoleMapper;
    private final AuthUserRoleMapper authUserRoleMapper;
    private final AuthRolePermissionMapper authRolePermissionMapper;

    public RoleDomainService(AuthRoleMapper authRoleMapper,
                             AuthUserRoleMapper authUserRoleMapper,
                             AuthRolePermissionMapper authRolePermissionMapper) {
        this.authRoleMapper = authRoleMapper;
        this.authUserRoleMapper = authUserRoleMapper;
        this.authRolePermissionMapper = authRolePermissionMapper;
    }

    public List<RoleBO> getRolesByUserId(Long userId) {
        LambdaQueryWrapper<AuthUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthUserRole::getUserId, userId);
        List<AuthUserRole> userRoles = authUserRoleMapper.selectList(wrapper);

        List<Long> roleIds = userRoles.stream().map(AuthUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new java.util.ArrayList<RoleBO>();
        }

        List<AuthRole> roles = authRoleMapper.selectBatchIds(roleIds);
        return roles.stream().map(role -> {
            RoleBO bo = new RoleBO();
            bo.setId(role.getId());
            bo.setRoleName(role.getRoleName());
            bo.setRoleKey(role.getRoleKey());

            LambdaQueryWrapper<AuthRolePermission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.eq(AuthRolePermission::getRoleId, role.getId());
            List<AuthRolePermission> rolePerms = authRolePermissionMapper.selectList(permWrapper);
            bo.setPermissionIds(rolePerms.stream().map(AuthRolePermission::getPermissionId).collect(Collectors.toList()));

            return bo;
        }).collect(Collectors.toList());
    }

    public void addRole(RoleBO roleBO) {
        AuthRole role = new AuthRole();
        role.setRoleName(roleBO.getRoleName());
        role.setRoleKey(roleBO.getRoleKey());
        authRoleMapper.insert(role);

        if (roleBO.getPermissionIds() != null) {
            for (Long permId : roleBO.getPermissionIds()) {
                AuthRolePermission rp = new AuthRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permId);
                authRolePermissionMapper.insert(rp);
            }
        }
    }

    public void assignRoleToUser(Long userId, Long roleId) {
        AuthUserRole userRole = new AuthUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        authUserRoleMapper.insert(userRole);
    }

    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        LambdaQueryWrapper<AuthRolePermission> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(AuthRolePermission::getRoleId, roleId);
        authRolePermissionMapper.delete(deleteWrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                AuthRolePermission rp = new AuthRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                authRolePermissionMapper.insert(rp);
            }
        }
    }

    public void deleteRole(Long roleId) {
        authRoleMapper.deleteById(roleId);

        LambdaQueryWrapper<AuthRolePermission> permWrapper = new LambdaQueryWrapper<>();
        permWrapper.eq(AuthRolePermission::getRoleId, roleId);
        authRolePermissionMapper.delete(permWrapper);

        LambdaQueryWrapper<AuthUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(AuthUserRole::getRoleId, roleId);
        authUserRoleMapper.delete(userRoleWrapper);
    }

    public void updateRole(RoleBO roleBO) {
        AuthRole role = new AuthRole();
        role.setId(roleBO.getId());
        role.setRoleName(roleBO.getRoleName());
        role.setRoleKey(roleBO.getRoleKey());
        authRoleMapper.updateById(role);

        if (roleBO.getPermissionIds() != null) {
            assignPermissionsToRole(roleBO.getId(), roleBO.getPermissionIds());
        }
    }
}
