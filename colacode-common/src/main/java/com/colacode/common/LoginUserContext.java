package com.colacode.common;

import com.colacode.common.constants.WebConstants;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
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
        UserContext userContext = getUserContext();
        return userContext != null ? userContext.getUserId() : null;
    }

    public static Long requireLoginUserId() {
        Long loginUserId = getLoginUserId();
        if (loginUserId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        return loginUserId;
    }

    public static Long getLoginUserIdOrDefault(Long fallbackUserId) {
        Long loginUserId = getLoginUserId();
        return loginUserId != null ? loginUserId : fallbackUserId;
    }

    public static String getUsername() {
        UserContext userContext = getUserContext();
        return userContext != null ? userContext.getUsername() : null;
    }

    public static String getToken() {
        UserContext userContext = getUserContext();
        return userContext != null ? userContext.getToken() : null;
    }

    public static UserContext getUserContext() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        Long userId = parseLong(request.getHeader(LOGIN_ID_HEADER));
        String username = request.getHeader(WebConstants.LOGIN_USERNAME_HEADER);
        String token = getHeaderOrDefault(request, WebConstants.LOGIN_TOKEN_HEADER,
                request.getHeader(WebConstants.AUTHORIZATION_HEADER));
        String source = request.getHeader(WebConstants.LOGIN_SOURCE_HEADER);

        if (userId == null && !StringUtils.hasText(username) && !StringUtils.hasText(token) && !StringUtils.hasText(source)) {
            return null;
        }

        return new UserContext(userId, username, token, source);
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    private static Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        String value = request.getHeader(headerName);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
