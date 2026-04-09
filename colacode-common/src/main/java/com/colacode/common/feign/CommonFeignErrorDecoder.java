package com.colacode.common.feign;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.util.Date;

public class CommonFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 500) {
            return new RetryableException(
                    response.status(),
                    "下游服务暂时不可用: " + methodKey,
                    response.request().httpMethod(),
                    new Date(System.currentTimeMillis() + 200L),
                    response.request());
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
