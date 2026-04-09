package com.colacode.common.autoconfigure;

import com.colacode.common.handler.CommonGlobalExceptionHandler;
import com.colacode.common.web.TraceIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CommonWebAutoConfiguration {

    @Bean
    public CommonGlobalExceptionHandler commonGlobalExceptionHandler() {
        return new CommonGlobalExceptionHandler();
    }

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }
}
