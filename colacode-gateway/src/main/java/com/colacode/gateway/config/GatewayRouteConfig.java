package com.colacode.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    private final GatewayRouteProperties gatewayRouteProperties;

    public GatewayRouteConfig(GatewayRouteProperties gatewayRouteProperties) {
        this.gatewayRouteProperties = gatewayRouteProperties;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        for (GatewayRouteProperties.RouteDefinition definition : gatewayRouteProperties.getDefinitions()) {
            if (definition.getId() == null || definition.getUri() == null || definition.getPath() == null) {
                continue;
            }
            routes.route(definition.getId(), r -> r.path(definition.getPath()).uri(definition.getUri()));
        }
        return routes.build();
    }
}
