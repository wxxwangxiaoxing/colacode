package com.colacode.common.enums;

public enum ResultCodeEnum {

    SUCCESS(200, 20000, "操作成功"),
    BAD_REQUEST(400, 40000, "请求参数错误"),
    UNAUTHORIZED(401, 40100, "未登录或登录已过期"),
    FORBIDDEN(403, 40300, "无权限访问"),
    NOT_FOUND(404, 40400, "资源不存在"),
    TOO_MANY_REQUESTS(429, 42900, "请求过于频繁，请稍后再试"),
    SYSTEM_ERROR(500, 50000, "系统异常，请稍后重试"),
    ADMIN_REQUIRED(403, 40301, "仅管理员可执行此操作"),
    USER_ACCESS_FORBIDDEN(403, 40302, "无权限操作该用户"),
    USER_ROLE_VIEW_FORBIDDEN(403, 40303, "无权限查看该用户角色"),
    USER_PERMISSION_VIEW_FORBIDDEN(403, 40304, "无权限查看该用户权限"),
    USER_NOT_FOUND(404, 40401, "用户不存在"),
    USER_DISABLED(403, 40305, "账号已被禁用"),
    USERNAME_EXISTS(400, 40001, "用户名已存在"),
    CURRENT_USER_DELETE_FORBIDDEN(400, 40002, "不能删除当前登录用户"),
    CURRENT_USER_DISABLE_FORBIDDEN(400, 40003, "不能禁用当前登录用户"),
    LAST_ADMIN_DELETE_FORBIDDEN(400, 40004, "至少保留一个管理员账号"),
    LAST_ADMIN_DISABLE_FORBIDDEN(400, 40005, "至少保留一个启用中的管理员账号"),
    LOGIN_FAILED(400, 40006, "用户名或密码错误");

    private final int httpStatus;
    private final int code;
    private final String message;

    ResultCodeEnum(int httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
