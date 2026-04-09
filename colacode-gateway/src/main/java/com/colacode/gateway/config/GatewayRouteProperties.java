package com.colacode.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "colacode.gateway.routes")
public class GatewayRouteProperties {

    private List<RouteDefinition> definitions = new ArrayList<>();

    public List<RouteDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<RouteDefinition> definitions) {
        this.definitions = definitions;
    }

    public static class RouteDefinition {

        private String id;
        private String uri;
        private String path;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
