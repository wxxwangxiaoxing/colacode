package com.colacode.gateway.exception;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ResponseStatusException) {
            Integer statusCode = resolveStatusCode((ResponseStatusException) ex);
            if (statusCode != null) {
                response.setStatusCode(HttpStatus.resolve(statusCode));
            }
        }

        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("code", response.getStatusCode() != null ? response.getStatusCode().value() : 500);
        result.put("message", ex.getMessage() != null ? ex.getMessage() : "系统异常");

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.fromSupplier(() ->
                bufferFactory.wrap(JSON.toJSONBytes(result))
        ));
    }

    private Integer resolveStatusCode(ResponseStatusException ex) {
        try {
            Method method = ResponseStatusException.class.getMethod("getStatusCode");
            Object statusCode = method.invoke(ex);
            if (statusCode != null) {
                Method valueMethod = statusCode.getClass().getMethod("value");
                Object value = valueMethod.invoke(statusCode);
                if (value instanceof Integer) {
                    return (Integer) value;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Method method = ResponseStatusException.class.getMethod("getStatus");
            Object status = method.invoke(ex);
            if (status instanceof HttpStatus) {
                return ((HttpStatus) status).value();
            }
        } catch (Exception ignored) {
        }

        try {
            Method method = ResponseStatusException.class.getMethod("getRawStatusCode");
            Object value = method.invoke(ex);
            if (value instanceof Integer) {
                return (Integer) value;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
