package com.colacode.circle.application.feign;

import com.colacode.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "colacode-auth")
public interface AuthFeignClient {

    @GetMapping("/auth/user/info")
    Result<Map<String, Object>> getUserInfo();

    @PostMapping("/auth/user/listByIds")
    Result<List<Map<String, Object>>> listUsersByIds(@RequestBody List<Long> ids);
}
