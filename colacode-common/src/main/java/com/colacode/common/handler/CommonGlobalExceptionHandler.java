package com.colacode.common.handler;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(e.getHttpStatus());
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletResponse response) {
        response.setStatus(ResultCodeEnum.BAD_REQUEST.getHttpStatus());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCodeEnum.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletResponse response) {
        response.setStatus(ResultCodeEnum.BAD_REQUEST.getHttpStatus());
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCodeEnum.BAD_REQUEST, message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletResponse response) {
        response.setStatus(ResultCodeEnum.NOT_FOUND.getHttpStatus());
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.fail(ResultCodeEnum.NOT_FOUND, "接口不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        response.setStatus(ResultCodeEnum.BAD_REQUEST.getHttpStatus());
        log.warn("参数异常: {}", e.getMessage());
        return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        if (isNotLoginException(e)) {
            response.setStatus(ResultCodeEnum.UNAUTHORIZED.getHttpStatus());
            log.warn("未登录异常: {}", e.getMessage());
            return Result.fail(ResultCodeEnum.UNAUTHORIZED);
        }
        response.setStatus(ResultCodeEnum.SYSTEM_ERROR.getHttpStatus());
        log.error("系统异常", e);
        return Result.fail(ResultCodeEnum.SYSTEM_ERROR);
    }

    private boolean isNotLoginException(Exception e) {
        return "cn.dev33.satoken.exception.NotLoginException".equals(e.getClass().getName());
    }
}
