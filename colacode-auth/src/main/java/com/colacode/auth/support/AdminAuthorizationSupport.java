package com.colacode.auth.support;

import cn.dev33.satoken.stp.StpUtil;
import com.colacode.auth.config.AdminAuthorizationProperties;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdminAuthorizationSupport {

    private final AdminAuthorizationProperties adminAuthorizationProperties;

    public AdminAuthorizationSupport(AdminAuthorizationProperties adminAuthorizationProperties) {
        this.adminAuthorizationProperties = adminAuthorizationProperties;
    }

    public boolean hasAdminAccess() {
        if (!StpUtil.isLogin()) {
            return false;
        }
        for (String roleKey : adminAuthorizationProperties.getRoleKeys()) {
            if (StpUtil.hasRole(roleKey)) {
                return true;
            }
        }
        for (String permissionKey : adminAuthorizationProperties.getPermissionKeys()) {
            if (StpUtil.hasPermission(permissionKey)) {
                return true;
            }
        }
        return false;
    }

    public void assertAdminAccess() {
        if (!hasAdminAccess()) {
            throw new BusinessException(ResultCodeEnum.ADMIN_REQUIRED);
        }
    }

    public boolean canAccessUser(Long userId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        return Objects.equals(StpUtil.getLoginIdAsLong(), userId) || hasAdminAccess();
    }

    public void assertCanAccessUser(Long userId) {
        if (!canAccessUser(userId)) {
            throw new BusinessException(ResultCodeEnum.USER_ACCESS_FORBIDDEN);
        }
    }
}
