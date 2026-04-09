package com.colacode.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "colacode.auth.login-security")
public class LoginSecurityProperties {

    private int maxAttemptsPerMinute = 10;
    private int maxFailures = 5;
    private long attemptWindowSeconds = 60;
    private long failureWindowSeconds = 900;
    private long lockSeconds = 900;
}
