package com.colacode.gateway.filter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.colacode.gateway.config.GatewayAuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Configuration
public class LoginFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final GatewayAuthProperties gatewayAuthProperties;

    public LoginFilter(GatewayAuthProperties gatewayAuthProperties) {
        this.gatewayAuthProperties = gatewayAuthProperties;
    }

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    String requestPath = SaHolder.getRequest().getRequestPath();
                    List<String> excludePaths = gatewayAuthProperties.getExcludePaths();
                    for (String excludePath : excludePaths) {
                        if (PATH_MATCHER.match(excludePath, requestPath)) {
                            return;
                        }
                    }
                    StpUtil.checkLogin();
                });
    }
}
