package com.colacode.common.autoconfigure;

import com.colacode.common.feign.CommonFeignErrorDecoder;
import com.colacode.common.feign.CommonFeignProperties;
import com.colacode.common.feign.CommonFeignRequestInterceptor;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "feign.Feign")
@EnableConfigurationProperties(CommonFeignProperties.class)
public class CommonFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder commonFeignErrorDecoder() {
        return new CommonFeignErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public Retryer commonFeignRetryer(CommonFeignProperties properties) {
        if (!properties.getRetry().isEnabled()) {
            return Retryer.NEVER_RETRY;
        }
        return new Retryer.Default(
                properties.getRetry().getPeriod(),
                properties.getRetry().getMaxPeriod(),
                properties.getRetry().getMaxAttempts());
    }
    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor commonFeignRequestInterceptor(CommonFeignProperties properties) {
        return new CommonFeignRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Request.Options commonFeignRequestOptions(CommonFeignProperties properties) {
        return new Request.Options(properties.getConnectTimeout(), properties.getReadTimeout());
    }
}



