package com.colacode.common;

import com.colacode.common.constants.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class LoginUserContext {

    public static final String LOGIN_ID_HEADER = WebConstants.LOGIN_ID_HEADER;

    private LoginUserContext() {
    }

    public static Long getLoginUserId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String loginId = request.getHeader(LOGIN_ID_HEADER);
        if (!StringUtils.hasText(loginId)) {
            return null;
        }

        try {
            return Long.parseLong(loginId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Long getLoginUserIdOrDefault(Long fallbackUserId) {
        Long loginUserId = getLoginUserId();
        return loginUserId != null ? loginUserId : fallbackUserId;
    }
}
