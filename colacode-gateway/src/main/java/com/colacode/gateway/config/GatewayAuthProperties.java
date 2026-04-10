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
@ConfigurationProperties(prefix = "colacode.gateway.auth")
public class GatewayAuthProperties {

    private String loginHeaderName = "loginId";

    private List<String> excludePaths = new ArrayList<>();

}
