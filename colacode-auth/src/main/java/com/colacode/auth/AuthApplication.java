package com.colacode.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务应用启动类
 * 认证中心模块，提供用户、角色、权限管理等功能
 *
 * @author wxx
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ConfigurationPropertiesScan
@MapperScan("com.colacode.auth.infra.mapper")
public class AuthApplication {

    /**
     * 应用主入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
