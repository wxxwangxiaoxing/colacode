package com.colacode.common;

import com.colacode.common.enums.ResultCodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum) {
        return fail(resultCodeEnum, resultCodeEnum.getMessage());
    }

    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(ResultCodeEnum.SYSTEM_ERROR, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
