package com.colacode.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.colacode.common.constants.WebConstants;
import com.colacode.gateway.config.GatewayAuthProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginUserHeaderFilter implements GlobalFilter, Ordered {

    private final GatewayAuthProperties gatewayAuthProperties;

    public LoginUserHeaderFilter(GatewayAuthProperties gatewayAuthProperties) {
        this.gatewayAuthProperties = gatewayAuthProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(gatewayAuthProperties.getLoginHeaderName());
                    headers.remove(WebConstants.LOGIN_USERNAME_HEADER);
                    headers.remove(WebConstants.LOGIN_TOKEN_HEADER);
                    headers.remove(WebConstants.LOGIN_SOURCE_HEADER);
                })
                .build();

        if (!StpUtil.isLogin()) {
            return chain.filter(exchange.mutate().request(request).build());
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(WebConstants.AUTHORIZATION_HEADER);
        ServerHttpRequest authenticatedRequest = request.mutate()
                .headers(headers -> {
                    headers.set(gatewayAuthProperties.getLoginHeaderName(), String.valueOf(StpUtil.getLoginId()));
                    headers.set(WebConstants.LOGIN_SOURCE_HEADER, WebConstants.LOGIN_CONTEXT_SOURCE_GATEWAY);
                    if (StringUtils.hasText(authorization)) {
                        headers.set(WebConstants.LOGIN_TOKEN_HEADER, authorization);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
