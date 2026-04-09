package com.colacode.common.feign;

import com.colacode.common.constants.WebConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CommonFeignRequestInterceptor implements RequestInterceptor {

    private final CommonFeignProperties properties;

    public CommonFeignRequestInterceptor(CommonFeignProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isPropagateHeaders()) {
            return;
        }

        String traceId = MDC.get(WebConstants.TRACE_ID_MDC_KEY);
        if (traceId != null && !traceId.isBlank()) {
            template.header(WebConstants.TRACE_ID_HEADER, traceId);
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String loginId = request.getHeader(WebConstants.LOGIN_ID_HEADER);
            if (loginId != null && !loginId.isBlank()) {
                template.header(WebConstants.LOGIN_ID_HEADER, loginId);
            }
        }
    }
}
