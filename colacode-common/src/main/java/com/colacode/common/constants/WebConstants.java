package com.colacode.common.constants;

public final class WebConstants {

    public static final String LOGIN_ID_HEADER = "loginId";
    public static final String LOGIN_USERNAME_HEADER = "X-Login-Username";
    public static final String LOGIN_TOKEN_HEADER = "X-Login-Token";
    public static final String LOGIN_SOURCE_HEADER = "X-Login-Source";
    public static final String LOGIN_CONTEXT_SOURCE_GATEWAY = "gateway";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private WebConstants() {
    }
}
