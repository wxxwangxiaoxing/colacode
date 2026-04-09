package com.colacode.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Configuration
public class SentinelGatewayConfig {

    private final RateLimitProperties rateLimitProperties;

    public SentinelGatewayConfig(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
        if (!rateLimitProperties.isEnabled()) {
            GatewayRuleManager.loadRules(new HashSet<>());
            GatewayApiDefinitionManager.loadApiDefinitions(new HashSet<>());
            log.warn("Sentinel gateway rate limit is disabled");
            return;
        }
        initCustomizedApis();
        initGatewayRules();
        initBlockHandler();
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        for (RateLimitProperties.RouteRule routeRule : rateLimitProperties.getRouteRules()) {
            if (routeRule.getResource() == null || routeRule.getCount() == null) {
                continue;
            }
            rules.add(new GatewayFlowRule(routeRule.getResource())
                    .setCount(routeRule.getCount())
                    .setIntervalSec(routeRule.getIntervalSec()));
        }
        for (RateLimitProperties.ApiRule apiRule : rateLimitProperties.getApiRules()) {
            if (apiRule.getResource() == null || apiRule.getCount() == null) {
                continue;
            }
            rules.add(new GatewayFlowRule(apiRule.getResource())
                    .setResourceMode(com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                    .setCount(apiRule.getCount())
                    .setIntervalSec(apiRule.getIntervalSec()));
        }

        GatewayRuleManager.loadRules(rules);
        log.info("Loaded {} gateway rate-limit rules", rules.size());
    }

    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new LinkedHashSet<>();
        for (RateLimitProperties.ApiRule apiRule : rateLimitProperties.getApiRules()) {
            if (apiRule.getResource() == null || apiRule.getPattern() == null) {
                continue;
            }
            Set<ApiPredicateItem> predicateItems = new HashSet<>();
            predicateItems.add(new ApiPathPredicateItem()
                    .setPattern(apiRule.getPattern())
                    .setMatchStrategy(apiRule.getMatchStrategy()));
            ApiDefinition definition = new ApiDefinition(apiRule.getResource())
                    .setPredicateItems(predicateItems);
            definitions.add(definition);
        }
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("Loaded {} custom API definitions for rate limit", definitions.size());
    }

    private void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (serverWebExchange, ex) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(
                                "{\"success\": false, \"code\": 429, \"message\": \"请求太频繁，请稍后再试\"}"));
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
