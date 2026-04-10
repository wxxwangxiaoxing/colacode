package com.colacode.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "colacode.gateway.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private List<RouteRule> routeRules = new ArrayList<>();

    private List<ApiRule> apiRules = new ArrayList<>();

    @Setter
    @Getter
    public static class RouteRule {

        private String resource;

        private Double count;

        private Integer intervalSec = 1;

    }

    @Setter
    @Getter
    public static class ApiRule {

        private String resource;

        private String pattern;

        private Integer matchStrategy = 0;

        private Double count;

        private Integer intervalSec = 1;

    }
}
