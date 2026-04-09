package com.colacode.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "colacode.gateway.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private List<RouteRule> routeRules = new ArrayList<>();

    private List<ApiRule> apiRules = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RouteRule> getRouteRules() {
        return routeRules;
    }

    public void setRouteRules(List<RouteRule> routeRules) {
        this.routeRules = routeRules;
    }

    public List<ApiRule> getApiRules() {
        return apiRules;
    }

    public void setApiRules(List<ApiRule> apiRules) {
        this.apiRules = apiRules;
    }

    public static class RouteRule {

        private String resource;

        private Double count;

        private Integer intervalSec = 1;

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public Double getCount() {
            return count;
        }

        public void setCount(Double count) {
            this.count = count;
        }

        public Integer getIntervalSec() {
            return intervalSec;
        }

        public void setIntervalSec(Integer intervalSec) {
            this.intervalSec = intervalSec;
        }
    }

    public static class ApiRule {

        private String resource;

        private String pattern;

        private Integer matchStrategy = 0;

        private Double count;

        private Integer intervalSec = 1;

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Integer getMatchStrategy() {
            return matchStrategy;
        }

        public void setMatchStrategy(Integer matchStrategy) {
            this.matchStrategy = matchStrategy;
        }

        public Double getCount() {
            return count;
        }

        public void setCount(Double count) {
            this.count = count;
        }

        public Integer getIntervalSec() {
            return intervalSec;
        }

        public void setIntervalSec(Integer intervalSec) {
            this.intervalSec = intervalSec;
        }
    }
}
