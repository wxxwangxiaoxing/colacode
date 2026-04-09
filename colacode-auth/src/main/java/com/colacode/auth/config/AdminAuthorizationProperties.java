package com.colacode.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "colacode.auth.admin")
public class AdminAuthorizationProperties {

    private List<String> roleKeys = new ArrayList<>(Arrays.asList("admin"));
    private List<String> permissionKeys = new ArrayList<>(Arrays.asList(
            "auth:manage",
            "user:manage",
            "role:manage",
            "permission:manage"
    ));
}
