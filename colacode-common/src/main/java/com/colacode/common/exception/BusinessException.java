package com.colacode.common.exception;

import com.colacode.common.enums.ResultCodeEnum;

public class BusinessException extends RuntimeException {

    private final int httpStatus;
    private final int code;

    public BusinessException(String message) {
        this(ResultCodeEnum.BAD_REQUEST, message);
    }

    public BusinessException(int code, String message) {
        this(ResultCodeEnum.BAD_REQUEST.getHttpStatus(), code, message);
    }

    public BusinessException(ResultCodeEnum resultCodeEnum) {
        this(resultCodeEnum, resultCodeEnum.getMessage());
    }

    public BusinessException(ResultCodeEnum resultCodeEnum, String message) {
        this(resultCodeEnum.getHttpStatus(), resultCodeEnum.getCode(), message);
    }

    public BusinessException(int httpStatus, int code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }
}
