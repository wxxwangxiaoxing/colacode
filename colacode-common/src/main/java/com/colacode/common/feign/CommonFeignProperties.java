package com.colacode.common.feign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "colacode.feign")
public class CommonFeignProperties {

    private boolean propagateHeaders = true;
    private int connectTimeout = 3000;
    private int readTimeout = 10000;
    private Retry retry = new Retry();

    @Data
    public static class Retry {
        private boolean enabled = true;
        private long period = 200;
        private long maxPeriod = 1000;
        private int maxAttempts = 3;
    }
}


