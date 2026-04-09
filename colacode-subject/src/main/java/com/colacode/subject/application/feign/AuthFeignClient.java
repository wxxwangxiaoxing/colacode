package com.colacode.subject.application.feign;

import com.colacode.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "colacode-auth")
public interface AuthFeignClient {

    @PostMapping("/auth/user/listByIds")
    Result<List<Map<String, Object>>> listUsersByIds(@RequestBody List<Long> ids);
}
