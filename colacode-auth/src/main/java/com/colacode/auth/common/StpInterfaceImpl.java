package com.colacode.auth.common;

import cn.dev33.satoken.stp.StpInterface;
import com.colacode.auth.domain.service.UserDomainService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserDomainService userDomainService;

    public StpInterfaceImpl(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userDomainService.getPermissionsByUserId(Long.parseLong(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userDomainService.getRolesByUserId(Long.parseLong(loginId.toString()));
    }
}
